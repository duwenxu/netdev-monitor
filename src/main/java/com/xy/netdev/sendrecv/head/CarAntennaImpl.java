package com.xy.netdev.sendrecv.head;

import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.dzt.DztCtrlInterPrtcServiceImpl;
import com.xy.netdev.frame.service.dzt.DztQueryInterPrtcServiceImpl;
import com.xy.netdev.frame.service.modem.ModemPrtcServiceImpl;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author luo
 * @date 2021/3/30
 */
@Service
@Slf4j
public class CarAntennaImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData>{


    @Autowired
    private DztCtrlInterPrtcServiceImpl ctrlInterService;
    @Autowired
    private DztQueryInterPrtcServiceImpl queryInterService;

    @Override
    public void callback(FrameRespData respData, IParaPrtclAnalysisService iParaPrtclAnalysisService, IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService) {
        switch (respData.getOperType()) {
            case SysConfigConstant.OPREATE_QUERY_RESP:
                queryInterService.queryParaResponse(respData);
                break;
            case SysConfigConstant.OPREATE_CONTROL_RESP:
                ctrlInterService.ctrlParaResponse(respData);
                break;
            default:
                log.warn("设备:{},未知调制解调器参数类型:{}", respData.getDevNo(), respData.getCmdMark());
                break;
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
