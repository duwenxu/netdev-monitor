package com.xy.netdev.frame.base.service;

import com.xy.netdev.frame.entity.TransportEntity;

import java.util.Set;

/**
 * 设备通讯类
 * @author cc
 */
public interface DeviceSocketService<T extends TransportEntity> extends ProtocolRequestService<T>, ProtocolResponseService<T>{

    /**
     * 设备标记
     * @return 设备标识
     */
    String deviceMark();
}
