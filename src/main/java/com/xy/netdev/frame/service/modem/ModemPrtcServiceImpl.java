package com.xy.netdev.frame.service.modem;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 650型号 调制解调器
 *
 * @author duwenxu
 * @create 2021-03-11 14:23
 */
@Slf4j
@Component
public class ModemPrtcServiceImpl implements IParaPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    ISysParamService sysParamService;
    @Autowired
    private ModemInterPrtcServiceImpl modemInterPrtcService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        return modemInterPrtcService.queryParaResponse(respData);
    }

    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    /**
     * 调制解调器参数控制响应
     * @param  respData   协议解析响应数据
     * @return 响应结果数据
     */
    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        String orignData = respData.getReciveOrignData();
        String controlSuccessCode = sysParamService.getParaRemark1(SysConfigConstant.CONTROL_SUCCESS);
        String controlFailCode = sysParamService.getParaRemark1(SysConfigConstant.CONTROL_FAIL);
        if (controlSuccessCode.equals(orignData)) {
            respData.setRespCode("控制成功");
        } else if (controlFailCode.equals(orignData)) {
            respData.setRespCode("控制失败");
        } else {
            throw new IllegalStateException("调制解调器控制响应异常，数据字节：" + orignData);
        }
        return respData;
    }
}
