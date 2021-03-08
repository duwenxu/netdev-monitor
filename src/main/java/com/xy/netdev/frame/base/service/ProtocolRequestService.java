package com.xy.netdev.frame.base.service;

import com.xy.netdev.frame.bo.DataBodyPara;
import com.xy.netdev.frame.entity.TransportEntity;

/**
 * 协议请求接口
 * @author cc
 */
public interface ProtocolRequestService<T extends TransportEntity> {

    void request(T t);
}
