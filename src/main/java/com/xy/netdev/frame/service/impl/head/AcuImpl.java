package com.xy.netdev.frame.service.impl.head;

import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import org.springframework.stereotype.Service;

/**
 * 1.2米ACU天线
 */
@Service
public class AcuImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {
    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService,
                         IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService) {

    }

    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
        byte[] bytes = socketEntity.getBytes();
        //长度为69则为主动上报. 否则为参数
        if (bytes.length == 69){
        }
        return null;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        return frameReqData.getParamBytes();
    }
}
