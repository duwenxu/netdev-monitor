package com.xy.netdev.sendrecv.base.service;

import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.sendrecv.entity.SocketEntity;

public interface ProtocolPackService <Q extends SocketEntity, T extends FrameReqData, R extends FrameRespData>{

    /**
     * 数据拆包
     * @param q q
     * @param t t
     * @return
     */
    R unpack(Q q, R r);

    /**
     * 数据装包
     * @param t
     * @return
     */
    byte[] pack(T t);

}
