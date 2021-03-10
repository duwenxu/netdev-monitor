package com.xy.netdev.frame.base.service;

import com.xy.netdev.frame.entity.TransportEntity;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;

/**
 * 协议请求接口
 * @author cc
 */
public interface ProtocolRequestService<T extends TransportEntity> {

    void socketRequest(T t, ProtocolRequestEnum requestEnum);
}
