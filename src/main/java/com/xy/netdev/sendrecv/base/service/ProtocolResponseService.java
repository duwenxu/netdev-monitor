package com.xy.netdev.sendrecv.base.service;

import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.sendrecv.entity.SocketEntity;

/**
 * 协议响应接口
 * @author cc
 */
public interface ProtocolResponseService<T extends FrameRespData> {

    /**
     * socket 数据响应
     * @param socketEntity socket实体
     */
    void socketResponse(SocketEntity socketEntity);
}
