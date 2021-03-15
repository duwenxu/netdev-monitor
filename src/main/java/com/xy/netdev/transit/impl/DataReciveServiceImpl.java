package com.xy.netdev.transit.impl;

import com.alibaba.fastjson.JSONArray;
import com.xy.common.exception.BaseException;
import com.xy.common.util.DateUtils;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.*;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.monitor.bo.DevStatusInfo;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.bo.TransRule;
import com.xy.netdev.monitor.entity.AlertInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.service.IDevStatusReportService;
import com.xy.netdev.transit.IDataReciveService;
import com.xy.netdev.transit.util.DataHandlerHelper;
import com.xy.netdev.websocket.send.DevIfeMegSend;
import io.netty.buffer.Unpooled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.List;


/**
 * <p>
 * [transit]中转层收[frame]协议解析层实现
 * </p>
 *
 * @author tangxl
 * @since 2021-03-11
 */
@Service
public class DataReciveServiceImpl implements IDataReciveService {

    @Autowired
    IDevStatusReportService devStatusReportService;

    /**
     * 参数查询接收
     * @param  respData   协议解析响应数据
     */
    public void paraQueryRecive(FrameRespData respData) {
        respData.setAccessType(SysConfigConstant.ACCESS_TYPE_PARAM);
        respData.setOperType(SysConfigConstant.OPREATE_QUERY_RESP);
        if(DevParaInfoContainer.handlerRespDevPara(respData)){
            DevIfeMegSend.sendParaToDev(respData.getDevNo());//如果设备参数变化,websocet推前台
        }
        DevLogInfoContainer.handlerRespDevPara(respData);//记录日志
        DevIfeMegSend.sendLogToDev(respData.getDevNo());//操作日志websocet推前台
        handlerAlertInfo(respData);//处理报警信息
    }

    /**
     * 参数控制接收
     * @param  respData   协议解析响应数据
     */
    public void paraCtrRecive(FrameRespData respData) {
        respData.setAccessType(SysConfigConstant.ACCESS_TYPE_PARAM);
        respData.setOperType(SysConfigConstant.OPREATE_CONTROL_RESP);
        if(DevParaInfoContainer.handlerRespDevPara(respData)){
            DevIfeMegSend.sendParaToDev(respData.getDevNo());//如果设备参数变化,websocet推前台
        }
        DevLogInfoContainer.handlerRespDevPara(respData);//记录日志
        DevIfeMegSend.sendLogToDev(respData.getDevNo());//操作日志websocet推前台
        handlerAlertInfo(respData);//处理报警信息
    }

    /**
     * 接口查询接收
     * @param  respData   协议解析响应数据
     */
    public void interfaceQueryRecive(FrameRespData respData) {
        respData.setAccessType(SysConfigConstant.ACCESS_TYPE_INTERF);
        respData.setOperType(SysConfigConstant.OPREATE_QUERY_RESP);
        if(DevParaInfoContainer.handlerRespDevPara(respData)){
            DevIfeMegSend.sendParaToDev(respData.getDevNo());//如果设备参数变化,websocet推前台
        }
        DevLogInfoContainer.handlerRespDevPara(respData);//记录日志
        DevIfeMegSend.sendLogToDev(respData.getDevNo());//操作日志websocet推前台
        handlerAlertInfo(respData);//处理报警信息
    }

    /**
     * 处理报警信息
     * @param  respData   协议解析响应数据
     */
    private void handlerAlertInfo(FrameRespData respData){
        List<FrameParaData> params =  respData.getFrameParaList();
        params.forEach(param->{
            FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByCmd(param.getDevType(),param.getParaNo());
            String ruleStr =  paraInfo.getTransRule();
            List<TransRule> rules = null;
            //读取参数配置的状态转换规则
            try {
                rules = JSONArray.parseArray(ruleStr, TransRule.class);
            }catch(Exception e) {
                throw new BaseException("参数状态转换规则有误");
            }
            //根据参数对应设备状态类型结合参数值上报设备状态或告警信息
            String type = paraInfo.getAlertPara();
                rules.forEach(rule->{
                    if(rule.getInner().equals(param.getParaVal())) {
                        String outerStatus = rule.getOuter();
                        switch(type) {
                            case SysConfigConstant.DEV_STATUS_ALARM:
                                //参数返回值是否产生告警
                                if (outerStatus.equals(SysConfigConstant.RPT_DEV_STATUS_ISALARM_YES)) {
                                    BaseInfo baseInfo = BaseInfoContainer.getDevInfoByNo(respData.getDevNo());
                                    FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),respData.getCmdMark());
                                    String alertDesc = DataHandlerHelper.genAlertDesc(baseInfo,frameParaInfo);
                                    AlertInfo alertInfo = new AlertInfo().builder()
                                            .devType(respData.getDevType())
                                            .alertLevel(paraInfo.getAlertLevel())
                                            .devNo(respData.getDevNo())
                                            .alertTime(DateUtils.now())
                                            .ndpaNo(paraInfo.getParaNo())
                                            .alertDesc(alertDesc).build();
                                    DevAlertInfoContainer.addAlertInfo(alertInfo);
                                }
                                if(DevStatusContainer.setAlarm(respData.getDevNo(),outerStatus)){
                                    devStatusReportService.rptWarning(respData,outerStatus);
                                }
                                break;
                            case SysConfigConstant.DEV_STATUS_INTERRUPT:
                                //参数返回值是否恢复中断
                                if(DevStatusContainer.setInterrupt(respData.getDevNo(),outerStatus)){
                                    devStatusReportService.rptUnInterrupted(respData,outerStatus);
                                }
                                break;
                            case SysConfigConstant.DEV_STATUS_SWITCH:
                                //参数返回值是否启用主备
                                if(DevStatusContainer.setUseStandby(respData.getDevNo(),outerStatus)){
                                    devStatusReportService.rptUseStandby(respData,outerStatus);
                                }
                                break;
                            case SysConfigConstant.DEV_STATUS_STANDBY:
                                //参数返回主备状态
                                if(DevStatusContainer.setMasterOrSlave(respData.getDevNo(),outerStatus)){
                                    devStatusReportService.rptMasterOrSlave(respData,outerStatus);
                                }
                                break;
                            case SysConfigConstant.DEV_STATUS_MAINTAIN:
                                //参数返回设备工作状态
                                if(DevStatusContainer.setWorkStatus(respData.getDevNo(),outerStatus)){
                                    devStatusReportService.rptWorkStatus(respData,outerStatus);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                });
        });
    }


}
