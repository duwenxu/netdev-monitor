package com.xy.netdev.frame.service;


/**
 * <p>
 * 设备响应头处理接口
 * </p>
 *
 * @author tangxl
 * @since 2021-03-09
 */
public interface IDevRepHeadHandlerService {
    /**
     * 解析数据获取命令
     * @param  frameData   帧数据
     * @return  响应数据
     */
    String unPackForCmd(byte[] frameData);
    /**
     * 解析数据获取数据体
     * @param  frameData   帧数据
     * @return  数据体
     */
    String unPackForBody(byte[] frameData);

}
