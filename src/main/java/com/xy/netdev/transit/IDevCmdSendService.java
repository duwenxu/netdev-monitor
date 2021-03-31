package com.xy.netdev.transit;


import com.xy.netdev.monitor.bo.InterCtrlInfo;
import com.xy.netdev.monitor.entity.ParaInfo;

import java.util.List;

/**
 * <p>
 * 设备命令发往接口
 * </p>
 *
 * @author tangxl
 * @since 2021-03-12
 */
public interface IDevCmdSendService {
    /**
     * 参数查询发送
     * @param  devNo   设备编号
     * @param  cmdMark 命令标识
     */
    void paraQuerySend(String devNo,String cmdMark);

    /**
     * 参数控制发送
     * @param  devNo   设备编号
     * @param  cmdMark 命令标识
     * @param  paraVal 参数值
     */
    void paraCtrSend(String devNo,String cmdMark,String paraVal);

    /**
     * 接口查询发送
     * @param  devNo   设备编号
     * @param  cmdMark 命令标识
     */
    void interfaceQuerySend(String devNo,String cmdMark);

    /**
     * 接口设置发送
     * @param  interCtrlInfo 接口设置信息
     */
    void interfaceCtrSend(InterCtrlInfo interCtrlInfo);


}
