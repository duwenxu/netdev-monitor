package com.xy.netdev.frame.base.service;

import com.xy.netdev.frame.bo.DataBodyPara;

import java.util.List;

public interface ProtocolPackService {

    //查询
    void doQuery();

    //控制
    void doControl();

    //查询应答
    <T extends DataBodyPara> List<T> doQueryResult();

    //控制应答
    <T extends DataBodyPara> List<T> doControlResult();
}
