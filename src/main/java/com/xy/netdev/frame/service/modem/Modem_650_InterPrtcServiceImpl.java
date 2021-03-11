package com.xy.netdev.frame.service.modem;

import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author duwenxu
 * @create 2021-03-11 14:26
 */
@Slf4j
@Component
public class Modem_650_InterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Override
    public void queryPara(FrameReqData reqInfo) {

    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        return null;
    }
}
