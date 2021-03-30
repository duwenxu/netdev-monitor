package com.xy.netdev.frame.service.modemscmm;

import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * SCMM-2300调制解调器 参数协议内容解析
 *
 * @author duwenxu
 * @create 2021-03-30 13:51
 */
@Service
@Slf4j
public class ModemScmmPrtcServiceImpl implements IParaPrtclAnalysisService {
    @Autowired
    private IDataReciveService dataReciveService;
    @Autowired
    private SocketMutualService socketMutualService;

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
