package com.xy.netdev.protocol.service;

import com.xy.netdev.protocol.model.DataBaseModel;
import com.xy.netdev.protocol.model.TransformBaseModel;

/**
 * 协议转换
 */
public interface ProtocolTransformService<T extends DataBaseModel, R extends TransformBaseModel> {


    /**
     * 原始数据转换成统一格式
     * @param t 原始数据
     * @return 统一格式
     */
    R originalDataTransform(T t);

}
