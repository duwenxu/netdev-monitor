package com.xy.netdev.frame.service.freqcov;

import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.codec.AscIIParamCodec;
import com.xy.netdev.frame.service.codec.BcdParamCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 6914变频器接口控制实现
 *
 * @author duwenxu
 * @create 2021-04-14 9:56
 */
@Service
@Slf4j
public class FreqConvertInterCtrlServiceImpl implements ICtrlInterPrtclAnalysisService {

    @Override
    public void ctrlPara(FrameReqData reqData) {

    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        return null;
    }
}
