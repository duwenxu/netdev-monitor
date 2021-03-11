package com.xy.netdev.transit;


import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.entity.TransportEntity;

/**
 * <p>
 * 数据发送接口
 * </p>
 *
 * @author tangxl
 * @since 2021-03-11
 */
public interface IDataSendService {
    /**
     * 参数查询发送
     * @param  devNo   设备编号
     * @param  paraNo  参数编号
     */
    void paraQuerySend(String devNo,String paraNo);

    /**
     * 参数控制发送
     * @param  devNo   设备编号
     * @param  paraNo  参数编号
     */
    void paraCtrSend(String devNo,String paraNo);

    /**
     * 接口查询发送
     * @param  devNo   设备编号
     * @param  paraNo  参数编号
     */
    void interfaceQuerySend(String devNo,String paraNo);


}
