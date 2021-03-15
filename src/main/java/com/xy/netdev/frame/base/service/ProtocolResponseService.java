package com.xy.netdev.frame.base.service;

import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.entity.SocketEntity;

/**
 * 协议响应接口
 * @author cc
 */
public interface ProtocolResponseService<T extends FrameRespData> {

    void socketResponse(SocketEntity socketEntity) throws Exception;
}
