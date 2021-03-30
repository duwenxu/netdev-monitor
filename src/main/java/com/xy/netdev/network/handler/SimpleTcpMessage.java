package com.xy.netdev.network.handler;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.frame.DeviceSocketSubscribe;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.xy.netdev.network.NettyUtil.SOCKET_QUEUE;


/**
 * TCP handler
 * @author cc
 */
@Slf4j
@ChannelHandler.Sharable
public class SimpleTcpMessage extends SimpleChannelInboundHandler<ByteBuf> {

    private final DeviceSocketSubscribe socketSubscribe = BeanFactoryUtil.getBean(DeviceSocketSubscribe.class);

    private final ExecutorService workExecutor = ThreadUtil.newSingleExecutor();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

        byte[] bytes = ByteBufUtil.getBytes(msg);
        InetSocketAddress socketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
        InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();
        SocketEntity socketEntity = SocketEntity.SocketEntityFactory.cloneable();
        socketEntity.setLocalPort(localAddress.getPort());
        socketEntity.setRemotePort(socketAddress.getPort());
        socketEntity.setRemoteAddress(socketAddress.getAddress().getHostAddress());
        socketEntity.setBytes(bytes);
        workExecutor.execute(() -> socketSubscribe.doResponse(socketEntity));
    }
}
