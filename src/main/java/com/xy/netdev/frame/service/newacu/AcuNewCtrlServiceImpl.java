package com.xy.netdev.frame.service.newacu;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.codec.DirectParamCodec;
import com.xy.netdev.frame.service.modemscmm.ModemScmmPrtcServiceImpl;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;
import static com.xy.netdev.common.util.ByteUtils.listToBytes;

/**
 * 7.3mACU接口控制实现
 *
 * @author duwenxu
 * @create 2021-08-09 14:17
 */
@Component
@Slf4j
public class AcuNewCtrlServiceImpl implements ICtrlInterPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private ModemScmmPrtcServiceImpl modemScmmPrtcService;
    @Autowired
    private IDataReceiveService dataReceiveService;


    @Override
    public void ctrlPara(FrameReqData reqData) {
        List<FrameParaData> paraList = reqData.getFrameParaList();
        if (paraList == null || paraList.isEmpty()) {
            return;
        }
        List<byte[]> ctlParamList = new ArrayList<>();
        /**组装控制参数bytes*/
        for (FrameParaData paraData : paraList) {
            byte[] frameBytes = modemScmmPrtcService.doGetFrameBytes(paraData);
            ctlParamList.add(frameBytes);
        }
        byte[] paramBytes = listToBytes(ctlParamList);
        reqData.setParamBytes(paramBytes);
        socketMutualService.request(reqData, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        String cmdMark = respData.getCmdMark();
        if (ObjectUtil.isNull(bytes)) {
            log.warn("7.3mACU天线设备控制响应异常, 未获取到数据体, 信息:{}", JSON.toJSONString(respData));
            return respData;
        }
        log.debug("7.3mACU天线设备接收到查询响应帧内容:[{}]", HexUtil.encodeHexStr(bytes));
        //获取接口单元的参数信息
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getInterLinkParaList(respData.getDevType(), cmdMark);
        List<FrameParaData> frameParaDataList = new ArrayList<>(frameParaInfos.size());
        //此处控制响应为每个参数对应一个字节
        int index = 0;
        for (FrameParaInfo param : frameParaInfos) {
            if (Objects.nonNull(param)) {
                //构造返回信息体 param
                FrameParaData paraInfo = new FrameParaData();
                BeanUtil.copyProperties(param, paraInfo, true);
                BeanUtil.copyProperties(respData, paraInfo, true);
                byte[] targetBytes = byteArrayCopy(bytes, index, 1);
                index++;
                Assert.isTrue(targetBytes != null && targetBytes.length > 0, String.format("7.3mACU天线设备编号为%s的参数字节长度为空", param.getParaNo()));
                DirectParamCodec directParamCodec = BeanFactoryUtil.getBean(DirectParamCodec.class);
                String respVal = directParamCodec.decode(targetBytes);
                paraInfo.setParaSetRes(respVal);
                paraInfo.setParaOrigByte(targetBytes);
                frameParaDataList.add(paraInfo);
            }
        }
        respData.setParamBytes(bytes);
        respData.setFrameParaList(frameParaDataList);
        dataReceiveService.interfaceCtrlRecive(respData);
        return respData;
    }


}
