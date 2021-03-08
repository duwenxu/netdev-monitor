package com.xy.netdev.frame.service;


import com.xy.netdev.frame.bo.DataBodyPara;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.entity.ParaInfo;

import java.util.List;

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
     * @param  devInfo    设备信息
     * @param  interInfo  接口信息
     */
    void queryPara(BaseInfo devInfo, Interface interInfo);
    /**
     * 查询响应协议
     * @param  devInfo   设备信息
     * @param  paraList  参数解析列表
     * @return  查询到的参数列表
     */
    List<DataBodyPara> queryParaResponse(BaseInfo devInfo,List<DataBodyPara> paraList);

}
