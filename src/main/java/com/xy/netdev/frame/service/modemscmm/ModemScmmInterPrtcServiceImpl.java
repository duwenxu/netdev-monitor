package com.xy.netdev.frame.service.modemscmm;

import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SCMM-2300调制解调器 接口协议内容解析
 *
 * @author duwenxu
 * @create 2021-03-30 14:30
 */
@Service
@Slf4j
public class ModemScmmInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Override
    public void queryPara(FrameReqData reqInfo) {

    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        return null;
    }
}
