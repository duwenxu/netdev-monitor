package com.xy.netdev.frame;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.FIFOCache;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.HexUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.rpt.service.StationControlHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

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
    private StationControlHandler stationControlHandler;

    @Autowired
    private List<AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData>> absSocketHandlerList;

    /**
     * 带时效的先进先出队列
     */
    private FIFOCache<String, AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData>> cache;

    /**
     * 线程池
     */
    private ThreadPoolExecutor executor;

    @PostConstruct
    public void init(){
        //队列
        cache = CacheUtil.newFIFOCache(absSocketHandlerList.size());
        //线程池工厂
        ThreadFactory factory = ThreadUtil.newNamedThreadFactory("ConsumerWork-", true);
        //cpu 核心数
        int cpuCore = Runtime.getRuntime().availableProcessors();
        //构建线程池
        executor = ExecutorBuilder.create()
                .setThreadFactory(factory)
                .setCorePoolSize(cpuCore)
                .setMaxPoolSize(cpuCore)
                .build();
        //执行消费线程
        consumer();
    }

    /**
     * 消费线程
     */
    private void consumer() {
       CompletableFuture.runAsync(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                CompletableFuture.runAsync(() -> {
                    try {
                        doResponse(SOCKET_QUEUE.take());
                    } catch (InterruptedException e) {
                        log.error("响应流程终端", e);
                    } catch (BaseException e) {
                        log.error("响应流程异常, 异常原因:{}", e.getMessage(), e);
                    }
                }, executor);
            }
        }, executor).exceptionally(throwable -> {
            log.error(throwable.getMessage());
            return null;
       });
    }

    /**
     * 执行响应流程
     * @param socketEntity socket实体
     */
    public void doResponse(SocketEntity socketEntity) throws BaseException{
        BaseInfo devInfo = getDevInfo(socketEntity.getRemoteAddress());
        //站控响应
        if (devInfo.getIsRptIp()!= null && Integer.parseInt(devInfo.getIsRptIp()) == 0){
            log.debug("收到站控数据, 远端地址:{}:{},数据体:{}"
                    , socketEntity.getRemoteAddress()
                    , socketEntity.getRemotePort()
                    , HexUtil.encodeHexStr(socketEntity.getBytes()).toUpperCase());
            stationControlHandler.stationControlReceive(socketEntity);
        }

        //执行设备数据响应
        Optional<AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData>> socketHandler
                = getHandler(socketEntity.getRemoteAddress());
        socketHandler.ifPresent(handler -> {
            log.debug("收到设备数据, 远端地址:{}:{},数据体:{}"
                    , socketEntity.getRemoteAddress()
                    , socketEntity.getRemotePort()
                    , HexUtil.encodeHexStr(socketEntity.getBytes()).toUpperCase());
            handler.socketResponse(socketEntity);
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
            log.warn("响应处理未找到指定设备信息, 执行方法getDevInfo(ip), 设备ip:{}", ip);
            return Optional.empty();
        }
        //设备网络协议
        String classByDevType = BaseInfoContainer.getClassByDevType(devInfo.getDevType());
        AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> handler = BeanFactoryUtil.getBean(classByDevType);
        cache.put(ip, handler, DateUnit.MINUTE.getMillis());
        return Optional.of(handler);
    }

}
