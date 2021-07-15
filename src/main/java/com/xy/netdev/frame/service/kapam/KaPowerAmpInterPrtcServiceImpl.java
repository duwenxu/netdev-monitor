package com.xy.netdev.frame.service.kapam;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.modem.ModemInterPrtcServiceImpl;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;

/**
 * Ka频段100W发射机 接口协议
 *
 * @author duwenxu
 * @create 2021-04-30 10:59
 */
@Service
@Slf4j
public class KaPowerAmpInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {
    /**控制响应标识*/
    private static final String RESPONSE_CMD = "10";
    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private ModemInterPrtcServiceImpl modemInterPrtcService;
    @Autowired
    private IDataReciveService dataReciveService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        log.info("Ka100W发射机收到查询响应帧内容：[{}]", HexUtil.encodeHexStr(bytes));
        if (ObjectUtil.isNull(bytes)) {
            log.warn("Ku100W发射机查询响应异常, 未获取到数据体, 设备编号：[{}], 信息:[{}]", respData.getDevNo(), JSON.toJSONString(respData));
            return respData;
        }
        String cmdMark = respData.getCmdMark();
        List<FrameParaData> frameParaDataList = new ArrayList<>();
        //获取接口单元的参数信息
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getInterLinkParaList(respData.getDevType(), cmdMark);
        for (FrameParaInfo param : frameParaInfos) {
            if (Objects.nonNull(param)) {
                //构造返回信息体 paraInfo
                Integer startPoint = param.getParaStartPoint();
                int byteLen = Integer.parseInt(param.getParaByteLen());
                byte[] targetBytes = new byte[0];
                try {
                    targetBytes = byteArrayCopy(bytes, startPoint, byteLen);
                } catch (Exception e) {
                    log.error("参数编号：[{}]字节长度截取错误，起始位置：{}，字节长度：{}", param.getParaNo(),startPoint,byteLen);
                }
                FrameParaData paraData = modemInterPrtcService.doGetParam(respData, targetBytes, param);
                frameParaDataList.add(paraData);
            }
        }
        respData.setFrameParaList(frameParaDataList);
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }
}
