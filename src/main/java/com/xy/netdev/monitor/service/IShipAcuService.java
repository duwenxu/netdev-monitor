package com.xy.netdev.monitor.service;

import com.xy.netdev.monitor.bo.Angel;

/**
 * 船载1.5米ACU 服务类
 *
 * @author admin
 * @date 2021-03-05
 */
public interface IShipAcuService{

    /**
     * 手动执行
     * @param angel
     */
    void operCtrl(Angel angel);

    /**
     * 自动执行
     * @param angel
     */
    void autoCtrl(Angel angel);

    /**
     * 获取当前位置的经纬度
     * @param angel
     * @return
     */
    Angel getLocalDeg(Angel angel);
}
