package com.xy.netdev.frame.service;

import com.xy.netdev.frame.entity.TransportEntity;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;

/**
 * 数据解析模块与其他模块通讯service
 * @author cc
 */
public interface SocketMutualService {

    /**
     * 外发数据请求接口
     * @param transportEntity
     * @param requestEnum
     */
    void request(TransportEntity transportEntity, ProtocolRequestEnum requestEnum);

}
