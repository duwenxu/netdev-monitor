package com.xy.netdev.frame.service;

import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;

/**
 * 控制接口协议解析接口 多参数
 * @author luo
 * @date 2021/3/26
 */
public interface ICtrlInterPrtclAnalysisService {

    /**
     * 控制协议
     * @param  reqInfo   请求参数信息
     */
    void ctrlPara(FrameReqData reqInfo);
    /**
     * 控制响应协议
     * @param  respData   协议解析响应数据
     * @return  响应数据
     */
    FrameRespData ctrlParaResponse(FrameRespData respData);

}
