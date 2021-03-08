package com.xy.netdev.frame;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.FIFOCache;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.thread.ThreadUtil;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.bo.DataBodyPara;
import com.xy.netdev.frame.entity.SocketEntity;
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
    private List<AbsDeviceSocketHandler<DataBodyPara>> absSocketHandlerList;

    /**
     * 带时效的先进先出队列
     */
    private FIFOCache<String, AbsDeviceSocketHandler<DataBodyPara>> cache;

    @PostConstruct
    public void init(){
        cache = CacheUtil.newFIFOCache(absSocketHandlerList.size());
        ThreadUtil.execute(() -> {
            try {
                while (true){
                    SocketEntity socketEntity = SOCKET_QUEUE.take();
                    AbsDeviceSocketHandler<DataBodyPara> deviceSocketHandler
                            = getHandler(socketEntity.getRemoteAddress(), absSocketHandlerList);
                    deviceSocketHandler.response(socketEntity);
                }
            } catch (InterruptedException e) {
                log.error("数据存储队列异常:", e);
            }
        });
    }

    /**
     * 根据key获取对应实体
     * @param key key
     * @param list 目标list
     * @return 目标实体
     */
    private AbsDeviceSocketHandler<DataBodyPara> getHandler(String key, List<AbsDeviceSocketHandler<DataBodyPara>> list){
        AbsDeviceSocketHandler<DataBodyPara> socketHandler = cache.get(key);
        if (socketHandler != null){
            return socketHandler;
        }
        AbsDeviceSocketHandler<DataBodyPara> handler = absSocketHandlerList.stream()
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到指定设备处理流程"));
        cache.put(key, handler, DateUnit.MINUTE.getMillis());
        return handler;
    }
}
