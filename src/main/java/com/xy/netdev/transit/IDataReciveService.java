package com.xy.netdev.transit;


import com.xy.netdev.frame.bo.FrameRespData;

/**
 * <p>
 * [transit]中转层收[frame]协议解析层接口
 * </p>
 *
 * @author tangxl
 * @since 2021-03-11
 */
public interface IDataReciveService {
    /**
     * 参数查询接收
     * @param  respData   协议解析响应数据
     */
    void paraQueryRecive(FrameRespData respData);

    /**
     * 参数控制接收
     * @param  respData   协议解析响应数据
     */
    void paraCtrRecive(FrameRespData respData);

    /**
     * 接口查询接收
     * @param  respData   协议解析响应数据
     */
    void interfaceQueryRecive(FrameRespData respData);


    /**
     * 接口控制接收
     * @param  respData   协议解析响应数据
     */
    void interfaceCtrlRecive(FrameRespData respData);


}
