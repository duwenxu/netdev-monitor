package com.xy.netdev.frame.service;


import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 参数协议解析接口  单参数
 * </p>
 *
 * @author tangxl
 * @since 2021-03-08
 */
public interface IParaPrtclAnalysisService {
    /**
     * 查询协议
     * @param  reqInfo   请求参数信息
     */
    void queryPara(FrameReqData reqInfo);
    /**
     * 查询响应协议
     * @param  respData   协议解析响应数据
     * @return  响应数据
     */
    FrameRespData queryParaResponse(FrameRespData respData);
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
