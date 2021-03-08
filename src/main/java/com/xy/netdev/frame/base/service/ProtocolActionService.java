package com.xy.netdev.frame.base.service;

import com.xy.netdev.frame.bo.DataBodyPara;
import com.xy.netdev.frame.entity.TransportEntity;

import java.util.List;

public interface ProtocolActionService {

    //查询
    void doQuery();

    //控制
    void doControl();

    //查询应答
    <T extends TransportEntity> List<T> doQueryResult();

    //控制应答
    <T extends TransportEntity> List<T> doControlResult();
}
