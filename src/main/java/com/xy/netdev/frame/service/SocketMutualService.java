package com.xy.netdev.frame.service;

import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.entity.TransportEntity;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;

import java.util.List;

/**
 * 数据解析模块与其他模块通讯service
 * @author cc
 */
public interface SocketMutualService {

    void request(TransportEntity transportEntity, ProtocolRequestEnum requestEnum);

}
