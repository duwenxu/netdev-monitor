package com.xy.netdev.network.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import lombok.extern.slf4j.Slf4j;


/**
 * 公用管道
 * @author cc
 */
@ChannelHandler.Sharable
@Slf4j
public class PipeLineHandler extends ChannelInitializer<DatagramChannel> {

    /**
     * netty channel 存储
     */

    private final ChannelHandler[] handlers;

    public PipeLineHandler(ChannelHandler[] handlers){
        this.handlers = handlers;
    }

    @Override
    protected void initChannel(DatagramChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        for (ChannelHandler handler : handlers) {
            pipeline.addLast(handler);
        }
    }


}
