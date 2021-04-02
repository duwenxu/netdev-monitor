package com.xy.netdev.network.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
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
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.FastThreadLocal;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
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
    private final int reconnection = 1;
    private final int retryCount = 10;
    private final MultithreadEventLoopGroup eventLoopGroup = Epoll.isAvailable() ? new EpollEventLoopGroup(1) :
            new NioEventLoopGroup(1);

    private CountDownLatch countDownLatch = new CountDownLatch(retryCount);

    /** 客户端请求的心跳命令  */
    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("xy", CharsetUtil.UTF_8));

    private final FastThreadLocal<Boolean> channelInactive = new FastThreadLocal<>();

    @Override
    public void run() {
        doConnect();
    }

    @SneakyThrows
    public void doConnect() {
        ChannelFuture channelFuture;
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(Epoll.isAvailable()? EpollSocketChannel.class: NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_LINGER, 0)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, reconnection * 1000)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addFirst(new IdleStateHandler(2, 2, 2, TimeUnit.SECONDS));
                        pipeline.addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                channelInactive.set(false);
                                HOST_CHANNEL_MAP.putIfAbsent(localPort, ctx);
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                log.warn("服务端强制断开连接, 在{}s之后准备尝试重连!", reconnection);
                                HOST_CHANNEL_MAP.remove(localPort);
                                channelInactive.set(true);
                                EventLoop eventLoop = ctx.channel().eventLoop();
                                eventLoop.schedule(NettyTcpClient.this::doConnect, reconnection, TimeUnit.SECONDS);
                                super.channelInactive(ctx);
                            }

                            /** 心跳 **/
                            @Override
                            public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
                                if (obj instanceof IdleStateEvent) {
                                    IdleStateEvent event = (IdleStateEvent) obj;
                                    if (IdleState.WRITER_IDLE.equals(event.state())) {
//                                        ctx.channel().writeAndFlush(HEARTBEAT_SEQUENCE.duplicate());
                                    }
                                }
                            }
                        });
                        if (handlers.length > 0){
                            for (ChannelHandler handler : handlers) {
                                pipeline.addLast(handler);
                            }
                        }
                    }
                });
        channelFuture = bootstrap.connect(new InetSocketAddress(host, port), new InetSocketAddress(localPort))
                .addListener((ChannelFuture futureListener) -> {
                if (futureListener.isSuccess()) {
                    log.info("TCP连接成功,本地端口:{} 连接地址{}:{}", localPort, host, port);
                    channelInactive.set(false);
                    countDownLatch = null;
                    countDownLatch = new CountDownLatch(retryCount);
                }
                if (!futureListener.isSuccess() && channelInactive.get()){
                    log.warn("与服务端{}:{}连接失败!{}s之后准备尝试重连!, 剩余重连次数{}", host, port, reconnection, countDownLatch.getCount());
                    final EventLoop eventLoop = futureListener.channel().eventLoop();
                    eventLoop.schedule(() ->{ countDownLatch.countDown(); this.doConnect(); }, reconnection, TimeUnit.SECONDS);
                }
        });
        try {
            channelFuture.channel().closeFuture().await();
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.info("TCP连接端口", e);
        }finally {
            channelInactive.remove();
            eventLoopGroup.shutdownGracefully();
            log.info("TCP 连接关闭");
        }
    }

}
