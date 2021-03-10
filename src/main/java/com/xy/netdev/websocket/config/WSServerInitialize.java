package com.xy.netdev.websocket.config;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * <p>
 * channel初始化配置类
 * </p>
 *
 * @author sunchao
 * @since 2020-09-04
 */
public class WSServerInitialize extends ChannelInitializer<SocketChannel> {

    /**
     * 初始化接收到的channel
     * @param ch
     * @throws Exception
     */
    @Override
    protected void initChannel(SocketChannel ch) throws Exception{
        ChannelPipeline pipeline = ch.pipeline();
        //WebSocket是基于Http协议的，要使用Http解编码器
        pipeline.addLast("http-decoder",new HttpServerCodec());
        //用于大数据流的分区传输
        pipeline.addLast("http-chunked",new ChunkedWriteHandler());
        //将多个消息转换为单一的request或者response对象，最终得到的是 FullHttpRequest对象
        pipeline.addLast("http-aggregator",new HttpObjectAggregator(1024 * 64));
        // 处理所有委托管理的 WebSocket 帧类型以及握手本身
        // 入参是 ws://server:port/context_path 中的 contex_path
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws",true,65536));
        //添加自定义处理函数
//        pipeline.addLast(new NettyHandler());
    }

}