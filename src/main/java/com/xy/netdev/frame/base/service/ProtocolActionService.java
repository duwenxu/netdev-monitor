package com.xy.netdev.frame.base.service;

import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;

public interface ProtocolActionService<T extends FrameReqData, R extends FrameRespData> {

    //查询
    void doQuery(T t);

    //控制
    void doControl(T t);

    //查询应答
    void doQueryResult(T t);

    //控制应答
    void doControlResult(T t);
}
