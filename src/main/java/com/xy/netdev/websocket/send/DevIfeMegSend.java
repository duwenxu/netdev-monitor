package com.xy.netdev.websocket.send;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.xy.netdev.common.util.ParaHandlerUtil;
import com.xy.netdev.container.*;
import com.xy.netdev.websocket.config.ChannelCache;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

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
            //此处加SerializerFeature.WriteMapNullValue是为了让数据中属性值为null的属性不被忽略
            //此处加SerializerFeature.DisableCircularReferenceDetect解决相同的对象序列化出错问题
            String msg = JSONObject.toJSONString(DevLogInfoContainer.getDevLogList(devNo),SerializerFeature.WriteMapNullValue,SerializerFeature.DisableCircularReferenceDetect);
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
            //此处加SerializerFeature.WriteMapNullValue是为了让数据中属性值为null的属性不被忽略
            //此处加SerializerFeature.DisableCircularReferenceDetect解决相同的对象序列化出错问题
            String msg = JSONObject.toJSONString(DevParaInfoContainer.getDevParaViewList(devNo), SerializerFeature.WriteMapNullValue,SerializerFeature.DisableCircularReferenceDetect);
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
            //此处加SerializerFeature.WriteMapNullValue是为了让数据中属性值为null的属性不被忽略
            //此处加SerializerFeature.DisableCircularReferenceDetect解决相同的对象序列化出错问题
            String msg = JSONObject.toJSONString(DevStatusContainer.getAllDevStatusInfoList(),SerializerFeature.WriteMapNullValue,SerializerFeature.DisableCircularReferenceDetect);
            TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(msg);
            channels.writeAndFlush(textWebSocketFrame);
        }
    }

    /**
     * 推送组合控制接口信息
     */
    public static void sendDevCtrlItfInfosToDev(String devNo){
        ChannelGroup channels = ChannelCache.getInstance().getChannels("DevCtrlItfInfos",devNo);
        if( channels != null){
            //此处加SerializerFeature.WriteMapNullValue是为了让数据中属性值为null的属性不被忽略
            //此处加SerializerFeature.DisableCircularReferenceDetect解决相同的对象序列化出错问题
            String msg = JSONObject.toJSONString(DevCtrlInterInfoContainer.getDevCtrInterList(devNo),SerializerFeature.WriteMapNullValue,SerializerFeature.DisableCircularReferenceDetect);
            TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(msg);
            channels.writeAndFlush(textWebSocketFrame);
        }
    }

    /**
     * 推送所有页面接口信息
     */
    public static void sendPageInfoToDev(String devNo,String cmdMark){
        ChannelGroup channels = ChannelCache.getInstance().getChannels("DevPageInfos",ParaHandlerUtil.genLinkKey(devNo, cmdMark));
        if( channels != null){
            String msg = PageInfoContainer.getPageInfo(devNo,cmdMark);
            TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(msg);
            channels.writeAndFlush(textWebSocketFrame);
        }
    }

    /**
     * 给指定设备编号  发送所有参数信息
     * @param devNo 设备编号
     */
    public static void sendAlertToDev(String devNo){
        ChannelGroup channels = null;
        String msg = "";
        if(StringUtils.isNotBlank(devNo)){
            channels = ChannelCache.getInstance().getChannels("DevAlertInfos",devNo);
            if(channels != null){
                //此处加SerializerFeature.WriteMapNullValue是为了让数据中属性值为null的属性不被忽略
                //此处加SerializerFeature.DisableCircularReferenceDetect解决相同的对象序列化出错问题
                msg = JSONObject.toJSONString(DevAlertInfoContainer.getDevAlertInfoList(devNo), SerializerFeature.WriteMapNullValue,SerializerFeature.DisableCircularReferenceDetect);
            }
        }else{
            channels = ChannelCache.getInstance().getChannelsByIfe("DevAlertInfos");
            if(channels != null){
                //此处加SerializerFeature.WriteMapNullValue是为了让数据中属性值为null的属性不被忽略
                //此处加SerializerFeature.DisableCircularReferenceDetect解决相同的对象序列化出错问题
                msg = JSONObject.toJSONString(DevAlertInfoContainer.getAllDevAlertInfoList(), SerializerFeature.WriteMapNullValue,SerializerFeature.DisableCircularReferenceDetect);
            }
        }
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(msg);
        channels.writeAndFlush(textWebSocketFrame);
    }

    /**
     * 发送第一次数据方法
     * @param interfaceMark 接口mark
     * @param devNo 设备编号
     */
    public static void sendFirstData(Object interfaceMark,Object devNo,Object cmdMark){
        switch (interfaceMark.toString()){
            case "DevLogInfos" :
                //发送日志信息
                sendLogToDev(devNo.toString());
                break;
            case "DevParaInfos" :
                //发送参数信息
                sendParaToDev(devNo.toString());
                break;
            case "DevCtrlItfInfos" :
                //发送组合控制接口信息
                sendDevCtrlItfInfosToDev(devNo.toString());
                break;
            case "DevPageInfos" :
                //发送页面信息接口数据
                sendPageInfoToDev(devNo.toString(),cmdMark.toString());
                break;
            case "DevAlertInfos" :
                sendAlertToDev(devNo.toString());
                break;
            default:
                sendDevStatusToDev();
                break;
        }
    }
}
