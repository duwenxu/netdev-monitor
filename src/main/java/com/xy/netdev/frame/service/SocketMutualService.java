package com.xy.netdev.frame.service;

import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.entity.TransportEntity;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;

import java.util.List;

/**
 * 数据解析模块与其他模块通讯service
 * @author cc
 */
public interface SocketMutualService {

   /**
    * 执行
    * @param <T> t
    * @param t t
    * @param requestEnum
    */
   <T extends TransportEntity> void request(T t, ProtocolRequestEnum requestEnum);

}
