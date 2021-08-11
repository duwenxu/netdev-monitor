package com.xy.netdev.frame.service.newacu;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.modemscmm.ModemScmmInterPrtcServiceImpl;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.xy.netdev.common.constant.SysConfigConstant.PARA_COMPLEX_LEVEL_COMPOSE;
import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;

/**
 * 7.3mACU接口查询解析实现
 *
 * @author duwenxu
 * @create 2021-08-09 14:15
 */
@Component
@Slf4j
public class AcuNewInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReceiveService dataReceiveService;
    @Autowired
    private ModemScmmInterPrtcServiceImpl modemScmmInterPrtcService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        //查询接口标识字
        String cmdMark = respData.getCmdMark();
        if (ObjectUtil.isNull(bytes)) {
            log.warn("7.3mACU天线设备查询响应异常, 未获取到数据体, 信息:{}", JSON.toJSONString(respData));
            return respData;
        }
        log.debug("7.3mACU天线设备接收到查询响应帧内容:[{}]", HexUtil.encodeHexStr(bytes));
        //获取接口单元的参数信息
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getInterLinkParaList(respData.getDevType(), cmdMark);
        List<FrameParaData> frameParaDataList = new ArrayList<>(frameParaInfos.size());
        for (FrameParaInfo param : frameParaInfos) {
            if (Objects.nonNull(param)){
                String cmplexLevel = param.getCmplexLevel();
                //构造返回信息体 paraInfo
                FrameParaData paraInfo = new FrameParaData();
                BeanUtil.copyProperties(param, paraInfo, true);
                BeanUtil.copyProperties(respData, paraInfo, true);
                Integer startPoint = param.getParaStartPoint();
                String byteLen = param.getParaByteLen();
                byte[] targetBytes = byteArrayCopy(bytes, startPoint, Integer.parseInt(byteLen));
                Assert.isTrue(targetBytes!=null&&targetBytes.length>0, String.format("7.3mACU天线设备编号为%s的参数字节长度为空", param.getParaNo()));
                //处理复杂参数
                if (PARA_COMPLEX_LEVEL_COMPOSE.equals(cmplexLevel)){
                    List<FrameParaInfo> subParaList = param.getSubParaList();
                    for (FrameParaInfo subPara : subParaList) {
                        //多个字节时获取字节配置位置
                        String remark1Data = subPara.getNdpaRemark1Data();
                        int byteIndex = 0;
                        if (StringUtils.isNoneBlank(remark1Data)){
                            byteIndex = Integer.parseInt(remark1Data);
                        }
                        byte[] subByte = ByteUtils.byteArrayCopy(targetBytes,byteIndex,1);
                        String value = modemScmmInterPrtcService.doGetValue(subPara, subByte);
                        FrameParaData subParaData = new FrameParaData();
                        BeanUtil.copyProperties(subPara, subParaData, true);
                        subParaData.setParaVal(value);
                        subParaData.setDevNo(respData.getDevNo());
                        frameParaDataList.add(subParaData);
                    }
                    //添加复杂参数  父参数
                    paraInfo.setParaVal(HexUtil.encodeHexStr(targetBytes).toUpperCase());
                    frameParaDataList.add(paraInfo);
                }else {
                    String value = modemScmmInterPrtcService.doGetValue(param, targetBytes);
                    paraInfo.setParaVal(value);
                    paraInfo.setParaOrigByte(targetBytes);
                    frameParaDataList.add(paraInfo);
                }
            }
        }
        //接口参数查询响应固定为 查询成功
        respData.setRespCode("0");
        respData.setFrameParaList(frameParaDataList);
        dataReceiveService.interfaceQueryRecive(respData);
        return respData;
    }
}
