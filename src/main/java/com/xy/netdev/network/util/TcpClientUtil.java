package com.xy.netdev.network.util;

import cn.hutool.core.io.BufferUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.socket.nio.NioClient;
import cn.hutool.socket.nio.NioServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class TcpClientUtil {

    public static void server(){
        NioServer server = new NioServer(8088);
        server.setChannelHandler((sc)->{
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            try{
                //从channel读数据到缓冲区
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    //Flips this buffer.  The limit is set to the current position and then
                    // the position is set to zero，就是表示要从起始位置开始读取数据
                    readBuffer.flip();
                    //eturns the number of elements between the current position and the  limit.
                    // 要读取的字节长度
                    byte[] bytes = new byte[readBuffer.remaining()];
                    //将缓冲区的数据读到bytes数组
                    readBuffer.get(bytes);
                    String body = StrUtil.utf8Str(bytes);
                    Console.log("[{}]: {}", sc.getRemoteAddress(), body);
                    doWrite(sc, body);
                } else if (readBytes < 0) {
                    IoUtil.close(sc);
                }
            } catch (IOException e){
                throw new IORuntimeException(e);
            }
        });
        server.listen();
    }

    public static void doWrite(SocketChannel channel, String response) throws IOException {
        response = "收到消息：" + response;
        //将缓冲数据写入渠道，返回给客户端
        channel.write(BufferUtil.createUtf8(response));
    }

    public static void client(){
        NioClient client = new NioClient("127.0.0.1", 8088);
        client.setChannelHandler((sc)->{
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            //从channel读数据到缓冲区
            int readBytes = sc.read(readBuffer);
            if (readBytes > 0) {
                //Flips this buffer.  The limit is set to the current position and then
                // the position is set to zero，就是表示要从起始位置开始读取数据
                readBuffer.flip();
                //returns the number of elements between the current position and the  limit.
                // 要读取的字节长度
                byte[] bytes = new byte[readBuffer.remaining()];
                //将缓冲区的数据读到bytes数组
                readBuffer.get(bytes);
                String body = StrUtil.utf8Str(bytes);
                Console.log("[{}]: {}", sc.getRemoteAddress(), body);
            } else if (readBytes < 0) {
                sc.close();
            }
        });
            client.listen();
       // 在控制台向服务器端发送数据
                Console.log("请输入发送的消息：");
                Scanner scanner = new Scanner(System.in);
                while (scanner.hasNextLine()) {
                    String request = scanner.nextLine();
                    if (request != null && request.trim().length() > 0) {
//                        client.write(BufferUtil.createUtf8(request));
                        client.write(ByteBuffer.wrap(HexUtil.decodeHex(request)));
                    }
                }
    }

    public static void main(String[] args) {
        ThreadUtil.execute(TcpClientUtil::server);
        client();
    }
}
