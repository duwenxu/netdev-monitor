package com.xy.netdev.frame.service;



import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.ParaInfo;
/**
 * <p>
 * 参数协议解析接口
 * </p>
 *
 * @author tangxl
 * @since 2021-03-08
 */
public interface IParaPrtclAnalysisService {
    /**
     * 查询协议
     * @param  devInfo   设备信息
     * @param  paraInfo  参数信息
     */
    void queryPara(BaseInfo devInfo,ParaInfo paraInfo);
    /**
     * 查询响应协议
     * @param  devInfo   设备信息
     * @param  paraInfo  参数信息
     * @return  响应数据
     */
    ParaInfo queryParaResponse(BaseInfo devInfo,ParaInfo paraInfo);
    /**
     * 控制协议
     * @param  devInfo   设备信息
     * @param  paraInfo  参数信息
     */
    void ctrlPara(BaseInfo devInfo,ParaInfo paraInfo);
    /**
     * 控制响应协议
     * @param  devInfo   设备信息
     * @param  paraInfo  参数信息
     * @return  响应数据
     */
    ParaInfo ctrlParaResponse(BaseInfo devInfo,ParaInfo paraInfo);
}