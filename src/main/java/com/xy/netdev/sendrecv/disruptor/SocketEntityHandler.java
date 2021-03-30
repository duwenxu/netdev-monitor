package com.xy.netdev.sendrecv.disruptor;

import com.lmax.disruptor.WorkHandler;
import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.frame.DeviceSocketSubscribe;
import com.xy.netdev.sendrecv.entity.SocketEntity;

/**
 * socket处理流程
 * @author cc
 */
public class SocketEntityHandler implements WorkHandler<SocketEntity> {

    private final DeviceSocketSubscribe deviceSocketSubscribe = BeanFactoryUtil.getBean(DeviceSocketSubscribe.class);

    @Override
    public void onEvent(SocketEntity event) throws Exception {
        deviceSocketSubscribe.doResponse(event);
    }
}
