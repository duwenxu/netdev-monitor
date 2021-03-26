package com.xy.netdev.sendrecv.base.service;

import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;

/**
 * 协议请求接口
 * @author cc
 */
public interface ProtocolRequestService<T extends FrameReqData> {

    void socketRequest(T t, ProtocolRequestEnum requestEnum);
}
