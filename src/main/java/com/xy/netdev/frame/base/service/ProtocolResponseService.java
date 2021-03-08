package com.xy.netdev.frame.base.service;

import com.xy.netdev.frame.bo.DataBodyPara;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.entity.TransportEntity;

/**
 * 协议响应接口
 * @author cc
 */
public interface ProtocolResponseService<T extends TransportEntity> {

    void response(SocketEntity socketEntity);
}
