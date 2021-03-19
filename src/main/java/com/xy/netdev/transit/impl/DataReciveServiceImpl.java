package com.xy.netdev.transit.impl;

import com.alibaba.fastjson.JSONArray;
import com.xy.common.exception.BaseException;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevLogInfoContainer;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.bo.TransRule;
import com.xy.netdev.rpt.service.IDevStatusReportService;
import com.xy.netdev.transit.IDataReciveService;
import com.xy.netdev.websocket.send.DevIfeMegSend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        //respData.setAccessType(SysConfigConstant.ACCESS_TYPE_PARAM);
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
            FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByNo(param.getDevType(),param.getParaNo());
            if(!paraInfo.getAlertPara().equals(SysConfigConstant.PARA_ALERT_TYPE_NULL)){
                String ruleStr =  paraInfo.getTransRule();
                List<TransRule> rules = null;
                //读取参数配置的状态转换规则
                try {
                    rules = JSONArray.parseArray(ruleStr, TransRule.class);
                }catch(Exception e) {
                    throw new BaseException("参数状态转换规则有误");
                }
                if(rules!=null){
                    devStatusReportService.reportWarningAndStaus(respData,rules,param);
                }
            }

        });
    }






}
