package com.xy.netdev.sendrecv.disruptor;

import com.lmax.disruptor.EventFactory;
import com.xy.netdev.sendrecv.entity.SocketEntity;

/**
 * socketEntity工厂
 * @author cc
 */
public class SocketEntityFactory implements EventFactory<SocketEntity> {
    @Override
    public SocketEntity newInstance() {
        return SocketEntity.SocketEntityFactory.cloneable();
    }
}
