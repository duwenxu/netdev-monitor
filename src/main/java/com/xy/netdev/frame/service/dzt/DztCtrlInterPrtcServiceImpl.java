package com.xy.netdev.frame.service.dzt;


import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 动中通--控制接口协议解析
 * @author luo
 * @date 2021/3/26
 */

@Service
@Slf4j
public class DztCtrlInterPrtcServiceImpl implements ICtrlInterPrtclAnalysisService {

    @Override
    public void ctrlPara(FrameReqData reqInfo) {

    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        return null;
    }
}
