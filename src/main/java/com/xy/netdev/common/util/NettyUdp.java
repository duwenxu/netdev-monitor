package com.xy.netdev.common.util;

import com.xy.netdev.socket.handler.PipeLineHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.*;


/**
 * udp数据发送服务
 * @author cc
 */
@Slf4j
public class NettyUdp implements Runnable {

    /**
     * 用于关闭netty
     */
    public static List<MultithreadEventLoopGroup> eventLoopGroupsList = Collections.synchronizedList(new ArrayList<>());

    private final ChannelHandler[] handles;

    private final Set<Integer> ports;

    public NettyUdp(Set<Integer> ports, ChannelHandler ...handles){
        this.handles = handles;
        this.ports = ports;
    }

    @Override
    @SneakyThrows
    public void run() {
        MultithreadEventLoopGroup loopGroup = Epoll.isAvailable() ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
        eventLoopGroupsList.add(loopGroup);
        try {
            String address = InetAddress.getLocalHost().getHostAddress();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(loopGroup)
                    .channel(Epoll.isAvailable()? EpollDatagramChannel.class: NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.IP_MULTICAST_TTL, 64)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(new PipeLineHandler(handles));
            if (Epoll.isAvailable()){
                bootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
            }
            Set<ChannelFuture> set = new HashSet<>();
            ports.forEach(port -> {
                set.add(bootstrap.bind(port).syncUninterruptibly());
                log.info("UDP服务启动, 地址{}:{}", address, port);
            });
            set.forEach(channelFuture -> {
                try {
                    channelFuture.channel().closeFuture().await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            log.error("udp服务启动失败", e);
        } finally {
            loopGroup.shutdownGracefully();
            log.warn("udp服务关闭");
        }
    }
}
