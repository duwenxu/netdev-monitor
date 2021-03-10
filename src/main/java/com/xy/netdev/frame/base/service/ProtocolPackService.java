package com.xy.netdev.frame.base.service;

import com.baomidou.mybatisplus.extension.api.R;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.entity.TransportEntity;

public interface ProtocolPackService <T extends SocketEntity, R extends TransportEntity>{

    /**
     * 数据拆包
     * @param t
     * @return
     */
    R unpack(T t);

    /**
     * 数据装包
     * @param r
     * @return
     */
    byte[] pack(R r);

}
