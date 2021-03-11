package com.xy.netdev.frame.service;

import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;

/**
 * 数据解析模块与其他模块通讯service
 * @author cc
 */
public interface SocketMutualService {

    /**
     * 外发数据请求接口
     * @param frameReqData
     * @param requestEnum
     */
    void request(FrameReqData frameReqData, ProtocolRequestEnum requestEnum);

}
