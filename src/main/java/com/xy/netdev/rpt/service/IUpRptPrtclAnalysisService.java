package com.xy.netdev.rpt.service;


import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.enums.StationCtlRequestEnums;

/**
 * <p>
 * 上报外部协议解析接口
 * </p>
 *
 * @author tangxl
 * @since 2021-03-08
 */
public interface IUpRptPrtclAnalysisService {
    /**
     * 上报外部协议
     * @param  headDev
     */
    void queryParaResponse(RptHeadDev headDev,StationCtlRequestEnums stationCtlRequestEnums);



}
