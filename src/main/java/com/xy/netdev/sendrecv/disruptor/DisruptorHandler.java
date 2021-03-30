package com.xy.netdev.sendrecv.disruptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.IgnoreExceptionHandler;
import com.lmax.disruptor.LiteTimeoutBlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Disruptor 队列支持
 * @author cc
 */
@Component
public class DisruptorHandler implements InitializingBean{


    private EventTranslatorOneArg<SocketEntity, SocketEntity> translator;

    private Disruptor<SocketEntity> disruptor;

    @Override
    public void afterPropertiesSet() throws Exception {
        //线程工厂
        ThreadFactory factory = ThreadUtil.newNamedThreadFactory("Disruptor-", true);

        //DisruptorEvent 赋值
        translator = (event, sequence, socketEntity) -> BeanUtil.copyProperties(socketEntity, event, true);

        //构建disruptor队列
        disruptor = new Disruptor<>(new SocketEntityFactory()
                , 1024
                , factory
                , ProducerType.SINGLE
                , new LiteTimeoutBlockingWaitStrategy(1, TimeUnit.SECONDS));

        //cpu 核心数
        int cpuCore = Runtime.getRuntime().availableProcessors();

        //多线程消费
        SocketEntityHandler[] handlers = ArrayUtil.newArray(SocketEntityHandler.class, cpuCore);
        for (int i = 0; i < cpuCore; i++) {
            handlers[i] = new SocketEntityHandler();
        }
        disruptor.setDefaultExceptionHandler(new IgnoreExceptionHandler());
        disruptor.handleEventsWithWorkerPool(handlers);
        disruptor.start();

    }

    /**
     * 数据推送至Disruptor队列
     * @param socketEntity socketEntity
     */
    public void push(SocketEntity socketEntity){
        RingBuffer<SocketEntity> ringBuffer = disruptor.getRingBuffer();
        ringBuffer.publishEvent(translator, socketEntity);
    }


}
