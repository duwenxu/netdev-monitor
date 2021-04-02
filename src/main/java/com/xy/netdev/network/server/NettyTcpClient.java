package com.xy.netdev.network.server;

import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.network.retry.RetryPolicy;
import com.xy.netdev.network.retry.RetryPolicyImpl;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static com.xy.netdev.network.NettyUtil.HOST_CHANNEL_MAP;


/**
 * tcp 连接
 * @author cc
 */
@Slf4j
public class NettyTcpClient implements Runnable {

    public NettyTcpClient(String host, int port, int localPort, ChannelHandler ...handlers){
        this.host = host;
        this.port = port;
        this.localPort = localPort;
        this.handlers = handlers;
    }
    /** tcp channel */

    private final int port;
    private final String host;
    private final int localPort;
    private final ChannelHandler[] handlers;
    private ChannelFuture channelFuture;
    private final FastThreadLocal<Integer> retryCount = new FastThreadLocal<Integer>(){
        @Override
        protected Integer initialValue() throws Exception {
            return 0;
        }
    };
    private final RetryPolicy retryPolicy = new RetryPolicyImpl();

    private final MultithreadEventLoopGroup eventLoopGroup = Epoll.isAvailable() ? new EpollEventLoopGroup(1) :
            new NioEventLoopGroup(1);


    @Override
    public void run() {
        doConnect();
    }

    @SneakyThrows
    public void doConnect() {
        Bootstrap bootstrap = setBootstrap();
        channelFuture = bootstrap.connect(new InetSocketAddress(host, port), new InetSocketAddress(localPort))
                        .addListener(future -> this.isSuccess());
        channelFuture.channel().closeFuture().await();
    }

    /**
     * Bootstrap set
     * @return Bootstrap
     */
    private Bootstrap setBootstrap() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(Epoll.isAvailable()? EpollSocketChannel.class: NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_LINGER, 0)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(channelInitializer());
        return bootstrap;
    }

    /**
     * channel 初始化
     * @return ChannelInitializer<SocketChannel>
     */
    private ChannelInitializer<SocketChannel> channelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addFirst(new IdleStateHandler(2, 2, 2, TimeUnit.SECONDS));
                pipeline.addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        retryCount.set(0);
                        HOST_CHANNEL_MAP.putIfAbsent(localPort, ctx);
                    }

                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        if (retryCount.get() == 0) {
                            ctx.disconnect();
                        }
                        if (retryPolicy.allowRetry(retryCount)) {
                            long sleepTimeMs = retryPolicy.getSleepTimeMs(retryCount);
                            log.warn("服务端强制断开连接, 在{}ms之后准备尝试重连!", sleepTimeMs);
                            HOST_CHANNEL_MAP.remove(localPort);
                            EventLoop eventLoop = ctx.channel().eventLoop();
                            eventLoop.schedule(NettyTcpClient.this::doConnect, sleepTimeMs, TimeUnit.MILLISECONDS);
                            super.channelInactive(ctx);
                        }

                        //现在处于不活动状态，调用ChannelInboundHandler的channelInactive
                        ctx.fireChannelInactive();
                    }
                });
                if (handlers.length > 0) {
                    for (ChannelHandler handler : handlers) {
                        pipeline.addLast(handler);
                    }
                }
            }
        };
    }

    /**
     * tcp 连接关闭
     */
    private void tcpClose() {
        eventLoopGroup.shutdownGracefully();
        log.info("TCP 连接关闭");
    }


    /**
     * 是否连接成功
     */
    private void isSuccess() {
        if (retryCount.get() == retryPolicy.getMaxRetries()){
            tcpClose();
            return;
        }
        if (channelFuture.isSuccess()) {
            log.info("TCP连接成功,本地端口:{} 连接地址{}:{}", localPort, host, port);
        }
        if (!channelFuture.isSuccess() && retryPolicy.allowRetry(retryCount)){
            long sleepTimeMs = retryPolicy.getSleepTimeMs(retryCount);
            final EventLoop eventLoop = channelFuture.channel().eventLoop();
            log.warn("与服务端{}:{}连接失败!{}m之后准备尝试重连!, 重连次数{}", host, port, sleepTimeMs, retryCount.get());
            eventLoop.schedule(this::doConnect, sleepTimeMs, TimeUnit.MILLISECONDS);
        }
    }

}
