package com.xy.netdev.frame.service.impl;

import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.entity.TransportEntity;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.SocketMutualService;
import org.springframework.stereotype.Service;

/**
 * 数据交互实现类
 * @author cc
 */
@Service
public class SocketMutualServiceImpl implements SocketMutualService {

    @Override
    public void request(TransportEntity transportEntity, ProtocolRequestEnum requestEnum) {
        String beanName = transportEntity.getDevInfo().getDevStatus();
        AbsDeviceSocketHandler<SocketEntity, TransportEntity> socketHandler = BeanFactoryUtil.getBean(beanName);
        socketHandler.socketRequest(transportEntity, requestEnum);
    }
}
