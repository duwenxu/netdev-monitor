package com.xy.netdev.transit;

import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.monitor.entity.AlertInfo;

/**
 * 设备告警事件上报
 *
 * @author luo
 * @date 2021-03-11
 */
public interface IDevAlarmReportService {


    /**
     * 生成报警信息
     */
     void generateAlarmInfo(FrameRespData respData);


}
