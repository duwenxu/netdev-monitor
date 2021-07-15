package com.xy.netdev.frame.service.shortwave;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.modemscmm.ModemScmmPrtcServiceImpl;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.rpt.enums.ShortWaveCmkEnum;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.impl.DataReciveServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.xy.netdev.common.constant.SysConfigConstant.PARA_COMPLEX_LEVEL_COMPOSE;
import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;
import static com.xy.netdev.common.util.ByteUtils.listToBytes;


/**
 * 750-400W短波设备(多参数控制)
 */
@Slf4j
@Component
public class ShortWaveInterCtrlServiceImpl implements ICtrlInterPrtclAnalysisService {
    @Autowired
    private ModemScmmPrtcServiceImpl modemScmmPrtcService;
    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private ShortWaveInterPrtcServiceImpl shortWaveInterPrtcService;
    @Autowired
    private DataReciveServiceImpl dataReciveService;

    public static final byte[] START_CHANNEL = new byte[]{0x00, 0x00, 0x00, 0x01};
    public static final byte[] SEND_DATA = new byte[]{0x00, 0x00, 0x00, 0x03};

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
        String devType = respData.getDevType();
        String devNo = respData.getDevNo();
        String cmdMark = respData.getCmdMark();
        /**命令序号*/
        byte[] serialNum = byteArrayCopy(bytes, bytes.length - 4, 4);
        byte[] paramsBytes = byteArrayCopy(bytes, 0, bytes.length - 4);

        List<FrameParaData> frameParaList = new ArrayList<>();
        if (ShortWaveCmkEnum.START_CHANNEL.getTempResCmk().equals(cmdMark)) {
            if (!Arrays.equals(serialNum, START_CHANNEL)) {
                log.error("400W短波设备建链应答帧序号错误：预期序号：[{}],收到序号：[{}]", HexUtil.encodeHex(START_CHANNEL), HexUtil.encodeHex(serialNum));
            }
            shortWaveInterPrtcService.addTempStatus(devType, paramsBytes, frameParaList, ShortWaveCmkEnum.START_CHANNEL, respData);
        } else if (ShortWaveCmkEnum.START_CHANNEL.getRespCmk().equals(cmdMark)) {
            if (!Arrays.equals(serialNum, START_CHANNEL)) {
                log.error("400W短波设备建链应答帧序号错误：预期序号：[{}],收到序号：[{}]", HexUtil.encodeHex(START_CHANNEL), HexUtil.encodeHex(serialNum));
            }
            List<FrameParaInfo> paraList = BaseInfoContainer.getInterLinkParaList(devType, ShortWaveCmkEnum.START_CHANNEL.getRespCmk());
            for (FrameParaInfo paraInfo : paraList) {
                if (PARA_COMPLEX_LEVEL_COMPOSE.equals(paraInfo.getCmplexLevel())) {
                    List<FrameParaInfo> subParaList = paraInfo.getSubParaList();
                    shortWaveInterPrtcService.addParamList(devNo, paramsBytes, frameParaList, subParaList, respData);
                    shortWaveInterPrtcService.addParentParam(respData, paramsBytes, frameParaList, paraInfo);
                } else {
                    shortWaveInterPrtcService.addParams(devNo, paramsBytes, frameParaList, paraInfo, respData);
                }
            }
        }
        respData.setFrameParaList(frameParaList);
        dataReciveService.paraCtrRecive(respData);
        return respData;
    }
}
