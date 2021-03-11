package com.xy.netdev.frame.base;

import com.xy.netdev.frame.base.service.DeviceSocketService;
import com.xy.netdev.frame.base.service.ProtocolActionService;
import com.xy.netdev.frame.entity.TransportEntity;

import java.util.Collections;

/**
 * 方法默认空实现
 * @author cc
 */
public abstract class DeviceSocketBaseHandler<R extends TransportEntity> implements DeviceSocketService<R>,
        ProtocolActionService<R> {


}
