package com.xy.netdev.rpt.service;

import com.xy.netdev.rpt.bo.RptBodyDev;

import java.util.List;

/**
 * 测站协议上报接口
 * @author cc
 */
public interface ReportService<T> {

    /**
     * 设备状态上报
     * @param lists
     */
    void statsReport(List<T> lists);


    /**
     * 告警时间上报
     * @param lists
     */
    void warnReport(List<T> lists);


}
