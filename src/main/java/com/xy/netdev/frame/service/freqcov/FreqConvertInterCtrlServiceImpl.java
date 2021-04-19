package com.xy.netdev.frame.service.freqcov;

import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.modem.ModemInterPrtcServiceImpl;
import com.xy.netdev.frame.service.modemscmm.ModemScmmPrtcServiceImpl;
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
 * 6914变频器接口控制实现
 *
 * @author duwenxu
 * @create 2021-04-14 9:56
 */
@Service
@Slf4j
public class FreqConvertInterCtrlServiceImpl implements ICtrlInterPrtclAnalysisService {
    @Autowired
    private ModemScmmPrtcServiceImpl modemScmmPrtcService;
    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private ModemInterPrtcServiceImpl modemInterPrtcService;
    @Autowired
    private IDataReciveService dataReciveService;

    @Override
    public void ctrlPara(FrameReqData reqData) {
        List<FrameParaData> paraList = reqData.getFrameParaList();
        byte[] bytes = new byte[]{};
        for (FrameParaData paraData : paraList) {
            byte[] frameBytes = modemScmmPrtcService.doGetFrameBytes(paraData);
            bytes = ByteUtils.bytesMerge(bytes, frameBytes);
        }
        reqData.setParamBytes(bytes);
        socketMutualService.request(reqData, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        List<FrameParaInfo> paraList = BaseInfoContainer.getInterLinkParaList(respData.getDevType(), respData.getCmdMark());
        List<FrameParaData> frameParaDataList = new ArrayList<>();
        for (FrameParaInfo param : paraList) {
            if (Objects.nonNull(param)) {
                //构造返回信息体 paraInfo
                Integer startPoint = param.getParaStartPoint();
                int byteLen = Integer.parseInt(param.getParaByteLen());
                byte[] targetBytes = byteArrayCopy(bytes, startPoint, byteLen);

                FrameParaData paraData = modemInterPrtcService.doGetParam(respData, targetBytes, param);
                frameParaDataList.add(paraData);
            }
        }
        respData.setFrameParaList(frameParaDataList);
        dataReciveService.paraCtrRecive(respData);
        return respData;
    }
}
