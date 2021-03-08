package com.xy.netdev.frame.base.service;

import com.xy.netdev.frame.entity.TransportEntity;

import java.util.Set;

/**
 * @author cc
 */
public interface DeviceSocketService<T extends TransportEntity> extends ProtocolRequestService<T>, ProtocolResponseService<T>{

    /**
     * 设备标记
     * @return 设备标识
     */
    String deviceMark();

    /**
     * 当前接收标识
     * @return 当前接收标识
     */
    String nowReceiveFlag();

    /**
     * 当前发送标识
     * @return 当前发送标识
     */
    String nowSendFlag();

    /**
     * 查询标识
     * @return 查询标识
     */
    Set<String> queryMark();

    /**
     * 控制标识
     * @return 控制标识
     */
    Set<String> controlMark();

    /**
     * 查询结果标识
     * @return 查询结果标识
     */
    Set<String> queryResultMark();

    /**
     * 控制结果标识
     * @return 控制结果标识
     */
    Set<String> controlResultMark();
}
