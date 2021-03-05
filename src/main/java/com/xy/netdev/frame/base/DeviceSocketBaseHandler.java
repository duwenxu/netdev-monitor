package com.xy.netdev.frame.base;

import com.xy.netdev.frame.bo.DataBodyPara;
import com.xy.netdev.frame.base.service.ProtocolPackService;
import com.xy.netdev.frame.base.service.DeviceSocketService;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 方法默认空实现
 * @author cc
 */
public abstract class DeviceSocketBaseHandler<R extends DataBodyPara> implements DeviceSocketService<R>, ProtocolPackService {
    @Override
    public void doQuery() { }

    @Override
    public void doControl() { }

    @Override
    public Set<String> queryMark() {
        return null;
    }

    @Override
    public Set<String> controlMark() {
        return null;
    }

    @Override
    public Set<String> queryResultMark() {
        return null;
    }

    @Override
    public Set<String> controlResultMark() {
        return null;
    }

    @Override
    public <T extends DataBodyPara> List<T> doQueryResult() {
        return Collections.emptyList();
    }

    @Override
    public <T extends DataBodyPara> List<T> doControlResult() {
        return Collections.emptyList();
    }
}
