package com.xy.netdev.network;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.HexUtil;
import com.alibaba.fastjson.JSONObject;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.network.enums.SocketTypeEnum;
import com.xy.netdev.network.server.NettyTcpClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.function.Function;



/**
 * @author cc
 */
@Slf4j
@Component
public class NettyHandler {

    /**
     * netty 数据缓存队列
     */
    public static final LinkedTransferQueue<SocketEntity> SOCKET_QUEUE = new LinkedTransferQueue<>();

    /**
     * netty channel 存储
     */
    public static final Map<Integer, ChannelHandlerContext> HOST_CHANNEL_MAP = new ConcurrentHashMap<>();

    /**
     * udp服务
     */
    public static Bootstrap udpBootstrap;

    /**
     * 发送数据
     * @param bytes 数据
     * @param localPort 本地端口
     * @param remoteIp 目标地址
     * @param remotePort 目标端口
     * @param sendType 发送类型
     * @return Optional<ChannelFuture>
     */
    public static Optional<ChannelFuture> sendMsg(byte[] bytes, int localPort, String remoteIp, int remotePort,
                                                  int sendType){
        if (SocketTypeEnum.TCP_CLIENT.getType().equals(sendType)){
            return sendTcpMsg(bytes, localPort, remoteIp, remotePort);
        }
        return sendUdpMsg(bytes, localPort, remoteIp, remotePort);
    }


    public static Optional<ChannelFuture> sendUdpMsg(byte[] bytes, int localPort, String remoteIp, int remotePort) {
        return sendCore(bytes, localPort, remoteIp, remotePort, byteBuf -> new DatagramPacket(byteBuf,
                new InetSocketAddress(remoteIp, remotePort)));
    }

    public static Optional<ChannelFuture> sendTcpMsg(byte[] bytes, int localPort, String remoteIp, int remotePort) {
        return sendCore(bytes, localPort, remoteIp, remotePort, byteBuf -> byteBuf);
    }

    private static Optional<ChannelFuture> sendCore(byte[] bytes, int localPort, String remoteIp, int remotePort,
                                                    Function<ByteBuf, Object> function) {
        ChannelHandlerContext ctx = HOST_CHANNEL_MAP.get(localPort);
        ChannelFuture channelFuture = null;
        if (ctx != null) {
            ByteBuf byteBuf = ctx.alloc().heapBuffer(bytes.length);
            if (!ctx.isRemoved() && ctx.channel().isWritable()){
                byteBuf.writeBytes(bytes);
                channelFuture = ctx.writeAndFlush(function.apply(byteBuf)).addListener(future -> {
                    if (!future.isSuccess()){
                        log.debug("数据发送失败, 本地端口{}, 远程地址{}, 远程端口{}, 数据体{}", localPort, remoteIp, remotePort, HexUtil.encodeHexStr(bytes));
                    }else {
                        log.debug("外发数据, 本地端口:{}, 目标地址:{}:{}, 数据体:{}", localPort, remoteIp, remotePort, HexUtil.encodeHexStr(bytes));
                    }
                });
            }
        } else {
            log.info("端口号未注册, 请检查端口正确性: {}", localPort);
        }
        return Optional.ofNullable(channelFuture);
    }


    /**
     * 关闭netty通道
     *
     * @param localPort 本地端口号
     */
    @SneakyThrows
    public synchronized static void nettyClose(int localPort) {
            ChannelHandlerContext context = HOST_CHANNEL_MAP.get(localPort);
            if (context != null) {
                HOST_CHANNEL_MAP.remove(localPort);
                context.channel().close().sync();
                log.info("下线通讯端口-->" + localPort + ", 当前在线channel--->" + JSONObject.toJSONString(HOST_CHANNEL_MAP));
            }
    }


    /**
     * 新增tcp连接
     * @param remoteHost 远程地址
     * @param remotePort 远程端口
     * @param localPort 本地端口
     */
    public static void tcpClientBind(String remoteHost, int remotePort, int localPort){
        nettyClose(localPort);
        ThreadUtil.execute(() -> {
            NettyTcpClient nettyTcpClient = new NettyTcpClient(remoteHost, remotePort, localPort);
            nettyTcpClient.run();
        });
    }

    /**
     * 新增upd地址
     * @param localPort 本地端口
     */
    public static void udpBind(int localPort) {
        if (udpBootstrap != null){
            nettyClose(localPort);
            udpBootstrap.bind(localPort).syncUninterruptibly();
            log.info("上线udp端口" + localPort + "当前在线channel--->" + JSONObject.toJSONString(HOST_CHANNEL_MAP));
        }else {
            throw new RuntimeException("UDP通讯主进程丢失, 请重启通讯服务器");
        }
    }

}

