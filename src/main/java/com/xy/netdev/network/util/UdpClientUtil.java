package com.xy.netdev.network.util;

import cn.hutool.core.util.HexUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

@Slf4j
public class UdpClientUtil {
    private static DatagramSocket clientSocket = null;
    private static InetSocketAddress serverAddress = null;
    /**编码格式，一般gbk或者utf-8*/
    private static String CHARSET_NAME="gbk";
    private static String UDP_URL="127.0.0.1";
    private static Integer UDP_PORT=60000;

    public static DatagramSocket getDatagramSocket()throws SocketException {
        return (clientSocket == null)?new DatagramSocket( ):clientSocket;
    }
    public static InetSocketAddress getInetSocketAddress(String host, int port)throws SocketException {
        return (serverAddress == null)?new InetSocketAddress(host, port):serverAddress;
    }

    public static void send(String host, int port,String msg) throws IOException {
        try {
            log.info("UDP发送数据:"+msg);
//            byte[] data = msg.getBytes(CHARSET_NAME);
            byte[] data = HexUtil.decodeHex(msg);
            DatagramPacket packet = new DatagramPacket(data, data.length, getInetSocketAddress(host,  port));
            getDatagramSocket().send(packet);
            log.info("发送完毕");
            getDatagramSocket().close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

//7F 31 00 10 01 01 03 00 04 00 00 01 01 01 04 02 BC 02 02 01 20 00 2F 01 E1 01 01 01 01 01 00 00 02 BC 01 01 02 17 00 06 01 E0 01 01 01 01 01 3F 7D
    public static void main(String[] args) throws Exception {
        //main方法用于测试
        for (int i = 0; i <5000 ; i++) {
            log.info(">>>>>>第"+i+"次发送UDP");
            UdpClientUtil.send(UdpClientUtil.UDP_URL, UdpClientUtil.UDP_PORT,"开始测试");
            //Thread.sleep(500);
        }

    }

}