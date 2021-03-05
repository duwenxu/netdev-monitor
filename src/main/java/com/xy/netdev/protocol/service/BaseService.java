package com.xy.netdev.protocol.service;

import com.xy.netdev.protocol.model.DataBaseModel;

public interface BaseService<T extends DataBaseModel> {

    //查询
    T query();

    //控制
    T control();

    //查询响应



    //控制响应
}
