package com.xy.netdev.transit;


import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.monitor.bo.DevStatusInfo;
import com.xy.netdev.monitor.entity.AlertInfo;

/**
 * 设备状态事件上报
 *
 * @author luo
 * @date 2021-03-11
 */
public interface IDevStatusReportService {

    /**
     * 生成报警信息
     */
    DevStatusInfo generateStatusInfo(FrameRespData respData);


    /**
     * 事件上报
     */
    void eventReport(DevStatusInfo statusInfoInfo);
}
