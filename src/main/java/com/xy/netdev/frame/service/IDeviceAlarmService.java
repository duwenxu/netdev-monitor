package com.xy.netdev.frame.service;

import com.xy.netdev.frame.bo.FrameRespData;

/**
 * 设备报警
 *
 * @author luo
 * @date 2021-03-05
 */
public interface IDeviceAlarmService {


    /**
     * 生成报警信息
     */
    void generateAlarmInfo(FrameRespData respData);
}
