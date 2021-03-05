package com.xy.netdev.protocol.service;

import com.xy.netdev.protocol.model.DataBaseModel;

public interface ProtocolHeadUnPackService<T extends DataBaseModel> {
    /**
     * 数据头拆箱
     * @param bytes 字节
     * @return t
     */
    T unPack(byte[] bytes);
}
