package com.xy.netdev.sendrecv.head;

import cn.hutool.core.util.StrUtil;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;

@Service
@Slf4j
public class KaBucImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {


    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService,
                         IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService, ICtrlInterPrtclAnalysisService ctrlInterPrtclAnalysisService) {
        frameRespData.setReciveOriginalData(StrUtil.str(frameRespData.getParamBytes(), Charset.defaultCharset()));
        if(iParaPrtclAnalysisService != null){
            iParaPrtclAnalysisService.queryParaResponse(frameRespData);
        }
        if(iQueryInterPrtclAnalysisService != null){
            iQueryInterPrtclAnalysisService.queryParaResponse(frameRespData);
        }
    }

    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
        return null;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        return new byte[0];
    }
}
