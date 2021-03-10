package com.xy.netdev.network.handler;

import cn.hutool.core.util.NetUtil;
import com.xy.netdev.frame.entity.SocketEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static com.xy.netdev.network.NettyHandler.HOST_CHANNEL_MAP;
import static com.xy.netdev.network.NettyHandler.SOCKET_QUEUE;


@Slf4j
@ChannelHandler.Sharable
public class SimpleUdpMessage extends SimpleChannelInboundHandler<DatagramPacket> {


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();
        int port = localAddress.getPort();
        HOST_CHANNEL_MAP.putIfAbsent(port, ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {

        ByteBuf byteBuf = msg.content();
        byte[] bytes = ByteBufUtil.getBytes(byteBuf);
        String remoteAddress= NetUtil.getIpByHost(msg.sender().getHostName());
        int remotePort = msg.sender().getPort();
        InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();

        SocketEntity socketEntity = new SocketEntity();
        socketEntity.setLocalPort(localAddress.getPort());
        socketEntity.setRemotePort(remotePort);
        socketEntity.setRemoteAddress(remoteAddress);
        socketEntity.setBytes(bytes);
        //数据放入队列
        SOCKET_QUEUE.offer(socketEntity, 1, TimeUnit.SECONDS);
    }

}
