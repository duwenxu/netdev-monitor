package com.xy.netdev.transit;

import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.monitor.entity.AlertInfo;

/**
 * 设备状态、事件告警上报
 *
 * @author luo
 * @date 2021-03-11
 */
public interface IDevInfoReportService {


    /**
     * 生成设备状态信息
     * @param respData
     */
    void generateStatusInfo(FrameRespData respData);


    /**
     * 生成报警信息
     */
     void generateAlarmInfo(FrameRespData respData);



}
