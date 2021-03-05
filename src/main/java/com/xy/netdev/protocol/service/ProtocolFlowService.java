package com.xy.netdev.protocol.service;

import com.xy.netdev.protocol.model.DataBaseModel;

/**
 * 协议流程
 * @author cc
 */
public interface ProtocolFlowService <T extends DataBaseModel>{

    /**
     * 获取类型标记
     * @return boolean
     */
    Boolean getFlag();

    /**
     * 执行流程
     * @param t extends DataBaseModel
     * @return extends DataBaseModel
     */
    T doAction(T t);
}
