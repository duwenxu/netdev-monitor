package com.xy.netdev.frame;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.FIFOCache;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.thread.ThreadUtil;
import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.monitor.entity.BaseInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

import static com.xy.netdev.container.BaseInfoContainer.getDevInfo;
import static com.xy.netdev.network.NettyUtil.SOCKET_QUEUE;

/**
 * 设备数据订阅
 * @author cc
 */
@Component
@Slf4j
public class DeviceSocketSubscribe {

    @Autowired
    private List<AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData>> absSocketHandlerList;

    /**
     * 带时效的先进先出队列
     */
    private FIFOCache<String, AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData>> cache;

    @PostConstruct
    public void init(){
        cache = CacheUtil.newFIFOCache(absSocketHandlerList.size());
        ThreadUtil.execute(() -> {
            try {
                while (true){
                    SocketEntity socketEntity = SOCKET_QUEUE.take();
                    //执行数据响应
                    getHandler(socketEntity.getRemoteAddress())
                            .ifPresent(handler -> handler.socketResponse(socketEntity));
                }
            } catch (InterruptedException e) {
                log.error("数据存储队列异常:", e);
            }
        });
    }

    /**
     * 根据key获取对应实体
     * @param ip ip
     * @return 目标实体
     */
    private Optional<AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData>> getHandler(String ip){
        AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> socketHandler = cache.get(ip);
        if (socketHandler != null){
            return Optional.of(socketHandler);
        }
        //设备信息
        BaseInfo devInfo = getDevInfo(ip);
        if (devInfo == null){
            log.error("位置设备数据, 设备ip:{}", ip);
            return Optional.empty();
        }
        //设备网络协议
        String classByDevType = BaseInfoContainer.getClassByDevType(devInfo.getDevType());
        AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> handler = BeanFactoryUtil.getBean(classByDevType);
        cache.put(ip, handler, DateUnit.MINUTE.getMillis());
        return Optional.of(handler);
    }
}
