package com.xy.netdev.protocol;

import com.xy.netdev.protocol.model.DataBaseModel;
import com.xy.netdev.protocol.model.TransformBaseModel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 设备参数处理基类
 * @author cc
 */
public class DeviceBaseHandler<T extends DataBaseModel, R extends TransformBaseModel> {

//    @Autowired
//    private DeviceService deviceService;


    /**
     * 收设备数据处理流程
     * @param t 非结构化数据
     */
    public void receiveDeviceDataAction(T t){

        //根据设备信息获取指定协议头结构信息

        //根据协议类型进行设备数据头解析

        //获取设备数据体部分进行转换成中心统一格式

        //数据回调发送中心
    }


    /**
     * 收本地数据流程处理
     * @param r 结构化数据
     */
    public void receiveLocalDataAction(R r){

        //根据r获取目标结构化信息

        //拼遥测参数

        //拼数据头

        //数据校验

        //数据发送
    }

    /**
     * 远端数据处理流程
     * @param t 非结构化数据
     */
    public void receiveRemoteToLocalDataAction(T t){

        //根据设备信息获取指定协议头结构信息

        //根据协议类型进行设备数据头解析

        //根据解析解析参数信息

        //根据解析结果查询对应设备参数

        //根据设备参数进行各个设备数据体拼装和头拼装流程

        //发送socket数据

        //执行本地数据流程
        receiveDeviceDataAction(t);


    }


    /**
     * 本地数据发送远端流程
     * @param r 结构化数据
     */
    public void receiveLocalToRemoteDataAction(R r){
        //从中心获取对应数据

        //根据协议拼装

        //socket 发送
    }






}
