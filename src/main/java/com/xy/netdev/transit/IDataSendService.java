package com.xy.netdev.transit;



/**
 * <p>
 * [transit]中转层发[frame]协议解析层接口
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
