package com.xy.netdev.frame.base.service;

import com.xy.netdev.frame.entity.TransportEntity;

public interface ProtocolActionService<T extends TransportEntity> {

    //查询
    void doQuery(T t);

    //控制
    void doControl(T t);

    //查询应答
    void doQueryResult(T t);

    //控制应答
    void doControlResult(T t);
}
