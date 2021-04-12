package com.xy.netdev.frame.service.msct;

import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import org.springframework.stereotype.Service;

/**
 * MSCT-102C多体制卫星信道终端站控协议  参数查询响应 帧协议解析层
 *
 * @author luo
 * @date 2021/4/8
 */
@Service
public class MsctPrtcServiceImpl implements IParaPrtclAnalysisService {

    @Override
    public void queryPara(FrameReqData reqInfo) {

    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        return null;
    }

    @Override
    public void ctrlPara(FrameReqData reqInfo) {

    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        return null;
    }
}
