package com.xy.netdev.frame.serivce;

import com.xy.netdev.frame.bo.DataBodyPara;

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
   <T extends DataBodyPara> void request(T t);

   /**
    * 回调
    * @param list
    * @param <T>
    */
   <T extends DataBodyPara> void callback(List<T> list);
}
