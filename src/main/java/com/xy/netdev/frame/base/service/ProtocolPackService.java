package com.xy.netdev.frame.base.service;

import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.entity.TransportEntity;

public interface ProtocolPackService {

    <T extends SocketEntity, R extends TransportEntity> R unpack(T t);

    <T extends SocketEntity, R extends TransportEntity> T pack(R r);
}
