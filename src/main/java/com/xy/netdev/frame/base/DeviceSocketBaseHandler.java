package com.xy.netdev.frame.base;

import com.xy.netdev.frame.base.service.DeviceSocketService;
import com.xy.netdev.frame.base.service.ProtocolActionService;
import com.xy.netdev.frame.entity.TransportEntity;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 方法默认空实现
 * @author cc
 */
public abstract class DeviceSocketBaseHandler<R extends TransportEntity> implements DeviceSocketService<R>, ProtocolActionService {
    @Override
    public <T extends TransportEntity> void doQuery(T t) { }

    @Override
    public <T extends TransportEntity> void doControl(T t) { }

    @Override
    public <T extends TransportEntity> List<T> doQueryResult(T t) {
        return Collections.emptyList();
    }

    @Override
    public <T extends TransportEntity> List<T> doControlResult(T t) {
        return Collections.emptyList();
    }
}
