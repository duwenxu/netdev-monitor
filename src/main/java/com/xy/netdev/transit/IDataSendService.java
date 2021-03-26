package com.xy.netdev.transit;


import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.transit.util.DataHandlerHelper;

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
     * @param  frameReqData   协议解析请求数据
     */
    void paraQuerySend(FrameReqData frameReqData);

    /**
     * 参数控制发送
     * @param  frameReqData   协议解析请求数据
     */
    void paraCtrSend(FrameReqData frameReqData);

    /**
     * 接口查询发送
     * @param  frameReqData   协议解析请求数据
     */
    void interfaceQuerySend(FrameReqData frameReqData);

    /**
     * 接口控制发送
     * @param  frameReqData   协议解析请求数据
     */
    void interfaceCtrlSend(FrameReqData frameReqData);

    /**
     * 通知网络传输结果
     * 网络层UDP链接异步回调后获取到传输结果后调用
     * @param  frameReqData   协议解析请求数据
     */
    void notifyNetworkResult(FrameReqData frameReqData);


}
