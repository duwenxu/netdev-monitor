package com.xy.netdev.sendrecv.head;

import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * MSCT-102C多体制卫星信道终端站控协议头解析
 *
 * @author luo
 * @date 2021/4/8
 */
@Service
@Slf4j
public class MsctImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {

    @Override
    public void callback(FrameRespData respData, IParaPrtclAnalysisService iParaPrtclAnalysisService, IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService, ICtrlInterPrtclAnalysisService ctrlInterPrtclAnalysisService) {

        if(respData.getOperType().equals(SysConfigConstant.OPREATE_CONTROL_RESP)) {
            if (ctrlInterPrtclAnalysisService != null) {
                ctrlInterPrtclAnalysisService.ctrlParaResponse(respData);
            } else {
                iParaPrtclAnalysisService.ctrlParaResponse(respData);
            }
        }else{
            if(iQueryInterPrtclAnalysisService!=null){
                iQueryInterPrtclAnalysisService.queryParaResponse(respData);
            }
        }
    }

    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData respData) {
        return null;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        return new byte[0];
    }
}
