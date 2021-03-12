package com.xy.netdev.websocket.send;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.xy.netdev.container.DevLogInfoContainer;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.websocket.config.ChannelCache;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

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
     * @param devNo 设备编号
     */
    public static void sendLogToDev(String devNo){
        ChannelGroup channels = ChannelCache.getInstance().getChannels("DevLogInfos",devNo);
        if( channels != null){
            String msg = JSONObject.toJSONString(DevLogInfoContainer.getDevLogList(devNo),SerializerFeature.WriteMapNullValue);
            TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(msg);
            channels.writeAndFlush(textWebSocketFrame);
        }
    }

    /**
     * 给指定设备编号  发送所有参数信息
     * @param devNo 设备编号
     */
    public static void sendParaToDev(String devNo){
        ChannelGroup channels = ChannelCache.getInstance().getChannels("DevParaInfos",devNo);
        if( channels != null){
            String msg = JSONObject.toJSONString(DevParaInfoContainer.getDevParaViewList(devNo), SerializerFeature.WriteMapNullValue);
            TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(msg);
            channels.writeAndFlush(textWebSocketFrame);
        }
    }

    /**
     * 推送所有设备状态
     */
    public static void sendDevStatusToDev(){
        ChannelGroup channels = ChannelCache.getInstance().getChannelsByIfe("DevStatusInfos");
        if( channels != null){
            String msg = JSONObject.toJSONString(DevStatusContainer.getAllDevStatusInfoList(),SerializerFeature.WriteMapNullValue);
            TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(msg);
            channels.writeAndFlush(textWebSocketFrame);
        }
    }

    /**
     * 发送第一次数据方法
     * @param interfaceMark 接口mark
     * @param devNo 设备编号
     */
    public static void sendFirstData(Object interfaceMark,Object devNo){
        if(devNo == null){
            sendDevStatusToDev();
            //推送所有设备的状态
        }else if(interfaceMark.toString().equals("DevLogInfos")){
            //发送日志信息
            sendLogToDev(devNo.toString());
        }else if(interfaceMark.toString().equals("DevParaInfos")){
            //发送参数信息
            sendParaToDev(devNo.toString());
        }
    }
}
