package com.xy.netdev.frame.service;


import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.entity.TransportEntity;

/**
 * <p>
 * 查询接口协议解析接口
 * </p>
 *
 * @author tangxl
 * @since 2021-03-08
 */
public interface IQueryInterPrtclAnalysisService {

    /**
     * 查询协议
     * @param  reqInfo    请求参数信息
     */
    void queryPara(FrameReqData reqInfo);
    /**
     * 查询响应协议
     * @param  transportEntity   数据传输对象
     * @return  协议解析响应数据
     */
    FrameRespData queryParaResponse(TransportEntity transportEntity);

}
