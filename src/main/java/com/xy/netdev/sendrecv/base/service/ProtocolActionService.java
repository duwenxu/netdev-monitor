package com.xy.netdev.sendrecv.base.service;

import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;

public interface ProtocolActionService<T extends FrameReqData, R extends FrameRespData> {

    /**
     * 查询
     * @param t t
     */
    void doQuery(T t);

    /**
     * 控制
     * @param t t
     */
    void doControl(T t);

    /**
     * 查询应答
     * @param t t
     */
    void doQueryResult(T t);

    /**
     * 控制应答
     * @param t 控制应答
     */
    void doControlResult(T t);
}
