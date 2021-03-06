package com.xy.netdev.websocket.send;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.xy.netdev.common.util.ParaHandlerUtil;
import com.xy.netdev.container.*;
import com.xy.netdev.monitor.bo.DevStatusInfo;
import com.xy.netdev.websocket.config.ChannelCache;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

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

    private static List<DevStatusInfo> oldDevStatuses = DevStatusContainer.getAllDevStatusInfoList();


    /**
     * 推送所有设备状态
     */
    public static void sendDevStatusToDev(){
        ChannelGroup channels = ChannelCache.getInstance().getChannelsByIfe("DevStatusInfos");
        List<DevStatusInfo> changedStatue = new CopyOnWriteArrayList<>();
        if( channels != null){
            List<DevStatusInfo> allDevStatusInfoList = DevStatusContainer.getAllDevStatusInfoList();
            //此处加SerializerFeature.WriteMapNullValue是为了让数据中属性值为null的属性不被忽略
            //此处加SerializerFeature.DisableCircularReferenceDetect解决相同的对象序列化出错问题
            for (DevStatusInfo devStatus : oldDevStatuses) {
                DevStatusInfo devStatusInfo = allDevStatusInfoList.stream().filter(status -> status.getDevNo().equals(devStatus.getDevNo())).collect(Collectors.toList()).get(0);
                if (!devStatusInfo.equals(devStatus)){
                    changedStatue.add(devStatus);
                }
            }
            if (!changedStatue.isEmpty()){
                log.info("当前设备状态改变的设备信息：[{}]",changedStatue);
                changedStatue.clear();
            }
            oldDevStatuses = allDevStatusInfoList;
//            List<DevStatusInfo> collect = allDevStatusInfoList.stream().filter(base -> base.getDevNo().equals("11")||base.getDevNo().equals("12")).collect(Collectors.toList());
//            log.debug("当前设备使用状态：{}:{}---{}:{}",collect.get(0).getDevNo(),collect.get(0).getMasterOrSlave(),collect.get(1).getDevNo(),collect.get(1).getMasterOrSlave());
            String msg = JSONObject.toJSONString(allDevStatusInfoList,SerializerFeature.WriteMapNullValue,SerializerFeature.DisableCircularReferenceDetect);
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
        if (channels!=null){
            channels.writeAndFlush(textWebSocketFrame);
        }
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
