package com.xy.netdev.transit.impl;

import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.DevLogInfoContainer;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.transit.IDataSendService;
import com.xy.netdev.transit.util.DataHandlerHelper;
import com.xy.netdev.websocket.send.DevIfeMegSend;
import org.springframework.stereotype.Service;

/**
 * <p>
 * [transit]中转层发[frame]协议解析层接口实现
 * </p>
 *
 * @author tangxl
 * @since 2021-03-11
 */
@Service
public class DataSendServiceImpl implements IDataSendService {

    /**
     * 参数查询发送
     * @param  frameReqData   协议解析请求数据
     */
    public void paraQuerySend(FrameReqData frameReqData) {
        frameReqData.setAccessType(SysConfigConstant.ACCESS_TYPE_PARAM);
        frameReqData.setOperType(SysConfigConstant.OPREATE_QUERY);
        DataHandlerHelper.getParaPrtclAnalysisService(frameReqData).queryPara(frameReqData);
        DevLogInfoContainer.handlerReqDevPara(frameReqData);//记录日志
        DevIfeMegSend.sendLogToDev(frameReqData.getDevNo());//操作日志websocet推前台
        handlerAlertInfo(frameReqData);//处理报警信息
    }

    /**
     * 参数控制发送
     * @param  frameReqData   协议解析请求数据
     */
    public void paraCtrSend(FrameReqData frameReqData) {
        frameReqData.setAccessType(SysConfigConstant.ACCESS_TYPE_PARAM);
        frameReqData.setOperType(SysConfigConstant.OPREATE_CONTROL);
        DataHandlerHelper.getParaPrtclAnalysisService(frameReqData).ctrlPara(frameReqData);
        DevLogInfoContainer.handlerReqDevPara(frameReqData);//记录日志
        DevIfeMegSend.sendLogToDev(frameReqData.getDevNo());//操作日志websocet推前台
        handlerAlertInfo(frameReqData);//处理报警信息
    }

    /**
     * 接口查询发送
     * @param  frameReqData   协议解析请求数据
     */
    public void interfaceQuerySend(FrameReqData frameReqData) {
        frameReqData.setAccessType(SysConfigConstant.ACCESS_TYPE_INTERF);
        frameReqData.setOperType(SysConfigConstant.OPREATE_QUERY);
        DataHandlerHelper.getQueryInterPrtclAnalysisService(frameReqData).queryPara(frameReqData);
        DevLogInfoContainer.handlerReqDevPara(frameReqData);//记录日志
        DevIfeMegSend.sendLogToDev(frameReqData.getDevNo());//操作日志websocet推前台
        handlerAlertInfo(frameReqData);//处理报警信息
    }
    /**
     * 处理报警信息
     * @param  frameReqData   协议解析请求数据
     */
    private void handlerAlertInfo(FrameReqData frameReqData){

    }
}
