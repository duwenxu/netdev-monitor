package com.xy.netdev.rpt.service;


import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.monitor.bo.TransRule;

import java.util.List;

/**
 * 设备状态上报服务
 */
public interface IDevStatusReportService {


    /**
     * 上报设备告警
     */
    void rptWarning(String devNo, String status);

    /**
     * 上报设备未中断，恢复连接
     */
    void rptUnInterrupted(String devNo, String status);

    /**
     * 上报设备中断
     */
    void rptInterrupted(String devNo, String status);

    /**
     * 上报设备启用主备
     */
    void rptUseStandby(String devNo, String status);

    /**
     * 上报设备主备状态
     */
    void rptMasterOrSlave(String devNo, String status);


    /**
     * 上报设备工作状态
     */
    void rptWorkStatus(String devNo, String status);

    /**
     * 上报告警和状态信息
     * @param respData
     * @param rules
     * @param param
     */
    void reportWarningAndStaus(FrameRespData respData, List<TransRule> rules, FrameParaData param);

}
