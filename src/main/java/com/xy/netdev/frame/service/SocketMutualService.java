package com.xy.netdev.frame.service;

import com.xy.netdev.frame.entity.TransportEntity;

import java.util.List;

/**
 * 数据解析模块与其他模块通讯service
 * @author cc
 */
public interface SocketMutualService {

   /**
    * 执行
    * @param t t
    * @param <T> t
    */
   <T extends TransportEntity> void request(T t);

   /**
    * 回调
    * @param list
    * @param <T>
    */
   <T extends TransportEntity> void callback(List<T> list);
}
