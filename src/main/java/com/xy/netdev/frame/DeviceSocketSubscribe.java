package com.xy.netdev.frame;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.FIFOCache;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.thread.ThreadUtil;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.entity.TransportEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;

import static com.xy.netdev.network.NettyHandler.SOCKET_QUEUE;

/**
 * 设备数据订阅
 * @author cc
 */
@Component
@Slf4j
public class DeviceSocketSubscribe {

    @Autowired
    private List<AbsDeviceSocketHandler<SocketEntity,TransportEntity>> absSocketHandlerList;

    /**
     * 带时效的先进先出队列
     */
    private FIFOCache<String, AbsDeviceSocketHandler<SocketEntity,TransportEntity>> cache;

    @PostConstruct
    public void init(){
        cache = CacheUtil.newFIFOCache(absSocketHandlerList.size());
        ThreadUtil.execute(() -> {
            try {
                while (true){
                    SocketEntity socketEntity = SOCKET_QUEUE.take();
                    AbsDeviceSocketHandler<SocketEntity,TransportEntity> deviceSocketHandler
                            = getHandler(socketEntity.getRemoteAddress());
                    //执行数据响应
                    deviceSocketHandler.socketResponse(socketEntity);
                }
            } catch (InterruptedException e) {
                log.error("数据存储队列异常:", e);
            }
        });
    }

    /**
     * 根据key获取对应实体
     * @param key key
     * @return 目标实体
     */
    private AbsDeviceSocketHandler<SocketEntity,TransportEntity> getHandler(String key){
        AbsDeviceSocketHandler<SocketEntity,TransportEntity> socketHandler = cache.get(key);
        if (socketHandler != null){
            return socketHandler;
        }
        AbsDeviceSocketHandler<SocketEntity,TransportEntity> handler = absSocketHandlerList.stream()
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到指定设备处理流程"));
        cache.put(key, handler, DateUnit.MINUTE.getMillis());
        return handler;
    }
}
