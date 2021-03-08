package com.xy.netdev.frame.service.impl;

import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.entity.TransportEntity;
import com.xy.netdev.frame.service.SocketMutualService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据交互实现类
 * @author cc
 */
@Service
public class SocketMutualServiceImpl implements SocketMutualService {


    @Override
    public <T extends TransportEntity> void request(T t) {
        AbsDeviceSocketHandler<T> socketHandler = BeanFactoryUtil.getBean("");
        socketHandler.request(t);
    }

    @Override
    public <T extends TransportEntity> void callback(List<T> list) {

    }
}
