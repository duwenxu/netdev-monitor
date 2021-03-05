package com.xy.netdev.socket.handler;

import cn.hutool.core.util.NetUtil;
import com.xy.netdev.protocol.model.DataBaseModel;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

import static com.xy.netdev.socket.NettyUtil.HOST_CHANNEL_MAP;


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
        int byteSize = byteBuf.readableBytes();
        byte[] bytes = new byte[byteSize];
        byteBuf.readBytes(bytes);
        String remoteAddress= NetUtil.getIpByHost(msg.sender().getHostName());
        int remotePort = msg.sender().getPort();
        InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();

        DataBaseModel dataBaseModel = new DataBaseModel();
        dataBaseModel.setLocalPort(localAddress.getPort());
        dataBaseModel.setRemotePort(remotePort);
        dataBaseModel.setRemoteAddress(remoteAddress);
        dataBaseModel.setOriginalReceiveBytes(bytes);
    }

}
