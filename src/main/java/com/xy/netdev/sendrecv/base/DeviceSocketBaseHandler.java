package com.xy.netdev.sendrecv.base;

import com.xy.netdev.sendrecv.base.service.DeviceSocketService;
import com.xy.netdev.sendrecv.base.service.ProtocolActionService;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;

/**
 * 方法默认空实现
 * @author cc
 */
public abstract class DeviceSocketBaseHandler<T extends FrameReqData, R extends FrameRespData> implements DeviceSocketService<T,
        R>,
        ProtocolActionService<T, R> {


}
