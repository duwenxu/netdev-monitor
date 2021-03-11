package com.xy.netdev.websocket.send;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xy.netdev.container.DevLogInfoContainer;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.websocket.config.ChannelCache;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * <p>
 * netty消息推送类
 * </p>
 *
 * @author sunchao
 * @since 2020-09-04
 */
@Slf4j
public class DevIfeMegSend {

    /**
     * 给指定设备编号  发送日志列表
     * @param devNo
     */
    public static void sendLogToDev(String devNo){
        ChannelGroup channels = ChannelCache.getInstance().getChannels("DevLogInfos",devNo);
        if( channels != null){
            String msg = JSONObject.toJSONString(DevLogInfoContainer.getDevLogList(devNo));
            TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(msg);
            channels.writeAndFlush(textWebSocketFrame);
        }
    }

    /**
     * 给指定设备编号  发送所有参数信息
     * @param devCode
     */
    public static void sendParaToDev(String devCode){
        ChannelGroup channels = ChannelCache.getInstance().getChannels("DevParaInfos",devCode);
        if( channels != null){
            String msg = JSONObject.toJSONString(DevParaInfoContainer.getDevParaViewList(devCode));
            TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(msg);
            channels.writeAndFlush(textWebSocketFrame);
        }
    }

    /**
     * 发送第一次数据方法
     * @param interfaceMark 接口mark
     * @param devCd 设备代号
     */
    public static void sendFirstData(Object interfaceMark,Object devCd){
        if(interfaceMark.toString().equals("DevLogInfos")){
            //发送日志信息
            sendLogToDev(devCd.toString());
        }else if(interfaceMark.toString().equals("DevParaInfos")){
            //发送参数信息
            sendParaToDev(devCd.toString());
        }
    }
}
