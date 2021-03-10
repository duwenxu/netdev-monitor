package com.xy.netdev.frame.base.service;

import com.xy.netdev.frame.entity.TransportEntity;


/**
 *
 * @author cc
 */
public interface DeviceSocketService<T extends TransportEntity> extends ProtocolRequestService<T>, ProtocolResponseService<T> {

}