package com.xy.netdev.frame.base.service;

import com.xy.netdev.frame.bo.DataBodyPara;

/**
 * 协议请求接口
 * @author cc
 */
public interface ProtocolRequestService<T extends DataBodyPara> {

    void request(T t);
}
