package com.xy.netdev.transit.impl;

import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.DevLogInfoContainer;
import com.xy.netdev.container.DevStatusContainer;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.rpt.service.IDevStatusReportService;
import com.xy.netdev.transit.IDataSendService;
import com.xy.netdev.transit.util.DataHandlerHelper;
import com.xy.netdev.websocket.send.DevIfeMegSend;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private IDevStatusReportService devStatusReportService;

    /**
     * 参数查询发送
     * @param  frameReqData   协议解析请求数据
     */
    public void paraQuerySend(FrameReqData frameReqData) {
        frameReqData.setAccessType(SysConfigConstant.ACCESS_TYPE_PARAM);
        frameReqData.setOperType(SysConfigConstant.OPREATE_QUERY);
        DataHandlerHelper.getParaPrtclAnalysisService(frameReqData).queryPara(frameReqData);
    }

    /**
     * 参数控制发送
     * @param  frameReqData   协议解析请求数据
     */
    public void paraCtrSend(FrameReqData frameReqData) {
        DataHandlerHelper.getParaPrtclAnalysisService(frameReqData).ctrlPara(frameReqData);
    }

    /**
     * 接口查询发送
     * @param  frameReqData   协议解析请求数据
     */
    public void interfaceQuerySend(FrameReqData frameReqData) {
        frameReqData.setAccessType(SysConfigConstant.ACCESS_TYPE_INTERF);
        frameReqData.setOperType(SysConfigConstant.OPREATE_QUERY);
        DataHandlerHelper.getQueryInterPrtclAnalysisService(frameReqData).queryPara(frameReqData);
    }
    /**
     * 接口控制发送
     * @param  frameReqData   协议解析请求数据
     */
    public void interfaceCtrlSend(FrameReqData frameReqData) {
        frameReqData.setAccessType(SysConfigConstant.ACCESS_TYPE_INTERF);
        frameReqData.setOperType(SysConfigConstant.OPREATE_CONTROL);
        DataHandlerHelper.getCtrlInterPrtclAnalysisService(frameReqData).ctrlPara(frameReqData);
    }
    /**
     * 处理报警信息
     * 
     * @param  frameReqData   协议解析请求数据
     */
    public void handlerAlertInfo(FrameReqData frameReqData){
//        String status = frameReqData.getIsOk();
//       //参数返回值是否产生中断
//        if(DevStatusContainer.setInterrupt(frameReqData.getDevNo(),status)){
//            devStatusReportService.rptInterrupted(frameReqData.getDevNo(),status);
//        }
    }

    /**
     * 通知网络传输结果
     * 网络层UDP链接异步回调后获取到传输结果后调用
     * @param  frameReqData   协议解析请求数据
     */
    public void notifyNetworkResult(FrameReqData frameReqData) {
        DevLogInfoContainer.handlerReqDevPara(frameReqData);//记录日志
        DevIfeMegSend.sendLogToDev(frameReqData.getDevNo());//操作日志websocet推前台
        handlerAlertInfo(frameReqData);//处理报警信息
    }
}
