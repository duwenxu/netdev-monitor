package com.xy.netdev;

import cn.hutool.core.util.HexUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 数据发送测试
 *
 * @author duwenxu
 * @create 2021-04-22 16:45
 */
@Slf4j
public class NettyUdpTest {

    private static final Bootstrap bootstrap = new Bootstrap();
    /**目的地address*/
    private static final InetSocketAddress DEST_ADDRESS = new InetSocketAddress("172.21.2.100", 5000);
    private static final InetSocketAddress SOURCE_ADDRESS = new InetSocketAddress("172.21.2.100", 8070);
    /**发送频率*/
    private static final int sendInterval = 2000;
    private static final String[] datas = new String[]{
            "7A7A7A7A000000150006250000222222227B7B7B7B",
            "7A7A7A7A00000018FDFD000600040000222222227B7B7B7B"
    };

    public static void sendFrameData() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .option(ChannelOption.IP_MULTICAST_TTL, 255)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(new SimpleChannelInboundHandler<DatagramPacket>(){
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
                            byte[] array = msg.content().array();
                            log.info("收到测试数据体：[{}]", HexUtil.encodeHexStr(array));
                        }
                    });

            Channel channel = bootstrap.bind(8070).sync().channel();
            while (true){
                for (String data : datas) {
                    DatagramPacket packet = new DatagramPacket(Unpooled.copiedBuffer(HexUtil.decodeHex(data)), DEST_ADDRESS);
                    Thread.sleep(sendInterval);
                    ChannelFuture channelFuture = channel.writeAndFlush(packet);
                    //添加ChannelFutureListener以便在写操作完成后接收通知
                    channelFuture.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            //写操作完成，并没有错误发生
                            if (future.isSuccess()){
                                log.info("向地址：[{}],端口：[{}]发送数据", packet.recipient().getAddress().getHostAddress(),packet.recipient().getPort());
                            }else{
                                //记录错误
                                System.out.println("error");
                                future.cause().printStackTrace();
                            }
                        }
                    });
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        sendFrameData();
    }
}
