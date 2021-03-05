package com.xy.netdev.frame.base.service;

import com.xy.netdev.frame.bo.DataBodyPara;

import java.util.Set;

public interface DeviceSocketService<T extends DataBodyPara> extends ProtocolRequestService<T>, ProtocolResponseService<T>{

    /**
     * 当前接收标识
     * @return 当前接收标识
     */
    String nowReceiveFlag();

    /**
     * 当前发送标识
     * @return 当前发送标识
     */
    String nowSendFlag();

    /**
     * 查询标识
     * @return 查询标识
     */
    Set<String> queryMark();

    /**
     * 控制标识
     * @return 控制标识
     */
    Set<String> controlMark();

    /**
     * 查询结果标识
     * @return 查询结果标识
     */
    Set<String> queryResultMark();

    /**
     * 控制结果标识
     * @return 控制结果标识
     */
    Set<String> controlResultMark();
}
