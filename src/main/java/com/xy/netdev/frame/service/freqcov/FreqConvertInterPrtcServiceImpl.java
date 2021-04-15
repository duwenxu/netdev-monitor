package com.xy.netdev.frame.service.freqcov;

import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 6914变频器接口查询实现
 *
 * @author duwenxu
 * @create 2021-04-14 9:53
 */
@Service
@Slf4j
public class FreqConvertInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {
    @Override
    public void queryPara(FrameReqData reqInfo) {

    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        return null;
    }
}
