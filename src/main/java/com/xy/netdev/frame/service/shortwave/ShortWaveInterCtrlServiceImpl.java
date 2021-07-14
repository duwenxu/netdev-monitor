package com.xy.netdev.frame.service.shortwave;

import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.modemscmm.ModemScmmPrtcServiceImpl;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.listToBytes;


/**
 * 750-400W短波设备(多参数控制)
 */
@Slf4j
@Component
public class ShortWaveInterCtrlServiceImpl implements ICtrlInterPrtclAnalysisService {

    /**临时应答关键字*/
    private static String TEMP_RESPONSE = "41";
    /**应答关键字*/
    private static String RESPONSE = "42";

    @Autowired
    private ModemScmmPrtcServiceImpl modemScmmPrtcService;

    @Autowired
    private SocketMutualService socketMutualService;

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
        String cmdMark = respData.getCmdMark();
        if (TEMP_RESPONSE.equals(cmdMark)){

        }else if (RESPONSE.equals(cmdMark)){

        }
        return null;
    }
}
