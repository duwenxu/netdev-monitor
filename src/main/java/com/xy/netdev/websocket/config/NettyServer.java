package com.xy.netdev.websocket.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * <p>
 * Netty服务端
 * </p>
 *
 * @author sunchao
 * @since 2020-09-04
 */
@Slf4j
@Order(5)
@Component
public class NettyServer  implements ApplicationRunner {

    /**
     * netty端口号
     */
    private String port;

    @Value("${spring.websocket.port}")
    public void setPort(String port){
        this.port = port;
    }

    private final EventLoopGroup mainGroup;
    private final EventLoopGroup subGroup;
    //启动辅助类：创建netty服务端
    private final ServerBootstrap server;
    private ChannelFuture future;

    public NettyServer() {
        mainGroup = new NioEventLoopGroup(1);
        subGroup = new NioEventLoopGroup();
        server = new ServerBootstrap();
        server.group(mainGroup, subGroup)
                //处理连接请求：生产channel
                .channel(NioServerSocketChannel.class)
                //连接请求基础配置
                .childHandler(new WSServerInitialize());
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        start();
    }

    public void start() {
        //绑定端口并开始接受传入连接
        this.future = server.bind(Integer.parseInt(port));
        log.info("Netty server started！");
    }
}
