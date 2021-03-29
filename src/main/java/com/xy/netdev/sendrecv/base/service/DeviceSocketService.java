package com.xy.netdev.sendrecv.base.service;

import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;


/**
 *
 * @author cc
 */
public interface DeviceSocketService<T extends FrameReqData, R extends FrameRespData> extends ProtocolRequestService<T>,
        ProtocolResponseService<R> {

}