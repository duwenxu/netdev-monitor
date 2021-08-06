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
import org.junit.Test;

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
    private static final InetSocketAddress DEST_ADDRESS = new InetSocketAddress("172.21.2.100", 8081);
    private static final InetSocketAddress SOURCE_ADDRESS = new InetSocketAddress("172.21.2.100", 8070);
    /**发送频率*/
    private static final int sendInterval = 1000;
    private static final String[] datas = new String[]{
            //2300调制解调器
            "7e1783020013592000fa00000056322e302d3139303932336d7e",
            "7e138303001351500056322e302d313930393233987e",
            "7e1f830500800000000000000000000000000000000000000000000000000801307e",
            "7e0a8306003e80000b02005e7e",
            "7e168307003e80000b1e000002030005002aaf54000000be7e",
            //6914射频设备
//            "7A7A7A7A000000150006140000222222227B7B7B7B",
//            "7A7A7A7A00000018FDFD000600040000222222227B7B7B7B",
            //650调制解调器
//            "02010665015301001f40005f03815f04815f05815f06815f07835f0800124f805f09815f0b012c5f0c001f40005f0d835f0e00124f805f0f815f10805f11815f12855f13805f14805f15805f16805f17815f18c0a800025f19ffffff005f1a815f1b815f1c815f1dc0a800025f1ec0a800025f1fff5f20c0a80002ffffffffffffffff5f21c0a80002ffffffffffffffff5f22c0a80002ffffffffffffffff5f23c0a80002ffffffffffffffff5f24c0a80002ffffffffffffffff5f25c0a80002ffffffffffffffff5f26c0a80002ffffffffffffffff5f27c0a80002ffffffffffffffff5f28805f29805f2a11115f2b14505f2c805f2d805f2e805f2f805f30805f31805f3525000a"
//    "7F19001001014000960202250125012501010101010101aa7d",
//            "7F07012000aa7d",
//            "7F07012100aa7d",
//            "7F07013200aa7d",
//            "7F07013100aa7d",
            //转换开关主备切换
//            "022210005306305f07305f08305f09315f10f15f11ff5fff20aa0a",
//            "022210005306305f07315f08315f09315f10f15f11ff5fff20aa0a",
            //
//            "7e211e0001003983 0130302e30022b3032392e350332302e3030050006ffffffff072020060508 03e8 e67e"
//            "0005000f0000000000000000000a0139070b0104000180"
            //Comtech响应帧
//            "2A9A3C"
            //712短波电台
//            "00800006061016152416AA",
//            "0081000720051109081012AA",
//            "0082000401111111AA",
//            "0083000117AA",
//            "00840004C0A80064",
//            "0085000112AA",
//            "0086000102AA",
//            "0087000103AA",
//            "0088000119AA",
//            "0089000103AA",
//            "008A000401000101AA",
//            "008B000101AA",
//            "008C000101AA",
//            "008D000101AA",
//            "008E00050200010403AA"
//            "55AA1202010001010200000000AAAA",
//            "55AA110100000000AAAA",
//            "55AA2201140124F8140124F800000001AAAA",
//            "55AA210100000001AAAA",
//            "55AA320100010100000002AAAA",
//            "55AA310100000002AAAA",
//            "55AA50020100010100000000AAAA"
    };

    @Test
    public void sendFrameData() {
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
                    channelFuture.addListener((ChannelFutureListener) future -> {
                        //写操作完成，并没有错误发生
                        if (future.isSuccess()){
                            log.info("向地址：[{}],端口：[{}]发送数据", packet.recipient().getAddress().getHostAddress(),packet.recipient().getPort());
                        }else{
                            //记录错误
                            System.out.println("error");
                            future.cause().printStackTrace();
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
    
}
