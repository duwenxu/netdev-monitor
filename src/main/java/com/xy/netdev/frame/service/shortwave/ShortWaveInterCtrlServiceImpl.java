package com.xy.netdev.frame.service.shortwave;

import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * 750-400W短波设备(多参数控制)
 */
@Slf4j
@Component
public class ShortWaveInterCtrlServiceImpl implements ICtrlInterPrtclAnalysisService {

    @Override
    public void ctrlPara(FrameReqData reqData) {

    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        return null;
    }
}
