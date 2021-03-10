package com.xy.netdev.frame.base.service;

import com.baomidou.mybatisplus.extension.api.R;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.entity.TransportEntity;

public interface ProtocolPackService {

    <T extends SocketEntity, R extends TransportEntity> R unpack(T t);

    <T extends TransportEntity> T pack(T t);
}
