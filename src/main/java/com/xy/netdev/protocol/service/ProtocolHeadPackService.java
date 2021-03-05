package com.xy.netdev.protocol.service;

import com.xy.netdev.protocol.model.DataBaseModel;

/**
 * 协议头装修
 * @author cc
 */
public interface ProtocolHeadPackService<T extends DataBaseModel> {

    /**
     * 数据装箱处理
     * @param t t
     * @return bytes
     */
    byte[] pack(T t);
}
