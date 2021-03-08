package com.xy.netdev.frame.base.service;

import com.xy.netdev.frame.entity.TransportEntity;

import java.util.List;

public interface ProtocolActionService {

    //查询
    <T extends TransportEntity>void doQuery(T t);

    //控制
    <T extends TransportEntity>void doControl(T t);

    //查询应答
    <T extends TransportEntity> List<T> doQueryResult();

    //控制应答
    <T extends TransportEntity> List<T> doControlResult();
}
