package com.xy.netdev.network.handler;

import cn.hutool.core.net.NetUtil;
import com.xy.netdev.frame.entity.SocketEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static com.xy.netdev.network.NettyUtil.SOCKET_QUEUE;


@Slf4j
@ChannelHandler.Sharable
public class SimpleTcpMessage extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

        byte[] bytes = ByteBufUtil.getBytes(msg);
        InetSocketAddress socketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
        InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();
        SocketEntity socketEntity = new SocketEntity();
        socketEntity.setLocalPort(localAddress.getPort());
        socketEntity.setRemotePort(socketAddress.getPort());
        socketEntity.setRemoteAddress(NetUtil.getIpByHost(socketAddress.getHostName()));
        socketEntity.setBytes(bytes);
        //数据放入队列
        SOCKET_QUEUE.offer(socketEntity, 1, TimeUnit.SECONDS);

    }
}
