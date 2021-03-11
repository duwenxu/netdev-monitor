package com.xy.netdev.websocket.config;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>
 * netty通道缓存类
 * </p>
 *
 * @author sunchao
 * @since 2020-09-04
 */
public class ChannelCache {

    /**
     * 所有通道信息缓存
     */
    private static final ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 接口和通道关系缓存
     */
    private static final ConcurrentMap<String, ChannelGroup> channelMapIfe = new ConcurrentHashMap();

    /**
     * 接口对应设备和通道缓存：当是多设备时，设备值为固定值all
     * ConcurrentMap：能够支持并发访问，线程安全，是一个高效的HashMap
     * 第一个String：接口标识；第二个String：设备
     */
    private static final ConcurrentMap<String, ConcurrentMap<String,ChannelGroup>> channelMap = new ConcurrentHashMap();

    private static ChannelCache channelCache;

    /**
     * 单实例缓存类
     */
    public static ChannelCache getInstance() {
        if(channelCache == null ){
            channelCache = new ChannelCache();
        }
        return channelCache;
    }

    /**
     * 存放通道信息
     * @param channel 通道
     */
    public void setChannel(Channel channel){
        clients.add(channel);
    }

    /**
     * 存放接口通道信息
     * @param channel 通道
     */
    public void setChannelByIfe(String ifeMark,Channel channel){
        if(channelMapIfe.get(ifeMark) != null){
            //同一个接口多个通道则在原来的通道组中增加
            channelMapIfe.get(ifeMark).add(channel);
        }else{
            //同一个接口第一个连接需要新建
            ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            clients.add(channel);
            channelMapIfe.put(ifeMark,clients);
        }
    }

    //存放接口、设备、通道信息
    public void setChannelUser(String ifeMake, String devCode,Channel channel){
        //判断此接口此设备是否已存在通道
        if(channelMap.get(ifeMake) != null && channelMap.get(ifeMake).get(devCode) != null){
            //存在，则将新的通道添加到通道组中
            channelMap.get(ifeMake).get(devCode).add(channel);
        }else{
            //不存在，则新建通道组填充进去
            ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            clients.add(channel);
            ConcurrentMap<String,ChannelGroup> map = new ConcurrentHashMap();
            map.put(devCode,clients);
            channelMap.put(ifeMake,map);
        }
    }

    /**
     * 移除通道信息
     * @param channel 通道
     */
    public void removeChannel(Channel channel){
        //移除通道组中的通道
        clients.remove(channel);
        //移除接口和通道缓存的关系
        Collection<ChannelGroup> col = channelMapIfe.values();
        for(ChannelGroup group:col){
            if(group.contains(channel)){
                col.remove(group);
                break;
            }
        }
        //移除接口设备和通道缓存的关系
        Collection<ConcurrentMap<String, ChannelGroup>> maps = channelMap.values();
        a:for(ConcurrentMap<String, ChannelGroup> map:maps){
            Collection<ChannelGroup> groups =  map.values();
            for(ChannelGroup group:groups){
                if(group.contains(channel)){
                    maps.remove(map);
                    //直接跳出最外层循环
                    break a;
                }
            }
        }
        channelMap.values().remove(channel);
        //关闭通道
        channel.disconnect();
    }


    /**
     * 通过接口和设备获取所有通道
     * @param ifeMake 接口标识
     * @param devCode 设备代号
     * @return 通道
     */
    public ChannelGroup getChannels(String ifeMake,String devCode){
        if(channelMap.get(ifeMake) != null){
            return channelMap.get(ifeMake).get(devCode);
        }
        return null;
    }

    /**
     * 获取指定接口下的所有通道
     * @param ifeMake 接口标识
     * @return 通道
     */
    public ChannelGroup getChannelsByIfe(String ifeMake){
        return channelMapIfe.get(ifeMake);
    }

    /**
     * 获取所有通道
     * @return 通道组：所有通道
     */
    public ChannelGroup getAllChannels(){
        return clients;
    }

    /**
     * 获取同一接口同一设备下的所有通道
     * @param ifeMark 接口标识
     * @return
     */
    public ChannelGroup getAllChannelByIfeMark(String ifeMark,String devCode){
        if(channelMap.get(ifeMark)!= null){
            return channelMap.get(ifeMark).get(devCode);
        }
        return null;
    }
}
