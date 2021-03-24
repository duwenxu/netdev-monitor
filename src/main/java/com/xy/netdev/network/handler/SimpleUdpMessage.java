package com.xy.netdev.network.handler;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.NetUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.monitor.entity.BaseInfo;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.xy.netdev.network.NettyUtil.HOST_CHANNEL_MAP;
import static com.xy.netdev.network.NettyUtil.SOCKET_QUEUE;


@Slf4j
@ChannelHandler.Sharable
public class SimpleUdpMessage extends SimpleChannelInboundHandler<DatagramPacket> {

    private final Set<String> ipFilter = BaseInfoContainer.getDevInfos().stream()
            .map(BaseInfo::getDevIpAddr)
            .collect(Collectors.toSet());


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();
        int port = localAddress.getPort();
        HOST_CHANNEL_MAP.putIfAbsent(port, ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
        String remoteAddress= NetUtil.getIpByHost(msg.sender().getHostName());
        //过滤非ip配置中的数据
        if (!ipFilter.contains(remoteAddress)){
            return;
        }
        InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();
        int remotePort = msg.sender().getPort();
        //过滤相同端口数据
        if (remotePort == localAddress.getPort()){
            return;
        }
        byte[] bytes = ByteBufUtil.getBytes(msg.content());

        SocketEntity socketEntity = new SocketEntity();
        socketEntity.setLocalPort(localAddress.getPort());
        socketEntity.setRemotePort(remotePort);
        socketEntity.setRemoteAddress(remoteAddress);
        socketEntity.setBytes(bytes);
        //数据放入队列
        SOCKET_QUEUE.offer(socketEntity, 1, TimeUnit.SECONDS);
    }


}
