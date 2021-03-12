package com.xy.netdev.transit.impl;

import com.alibaba.fastjson.JSONArray;
import com.xy.common.util.DateUtils;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevAlertInfoContainer;
import com.xy.netdev.container.DevLogInfoContainer;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.bo.TransRule;
import com.xy.netdev.monitor.entity.AlertInfo;
import com.xy.netdev.transit.IDataReciveService;
import com.xy.netdev.websocket.send.DevIfeMegSend;
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
            //读取参数配置的状态转换规则
            List<TransRule> rules = JSONArray.parseArray(ruleStr, TransRule.class);
            String status = paraInfo.getAlertPara();
            if(status.equals(SysConfigConstant.DEV_STATUS_ALARM)){
                rules.forEach(rule->{
                    if(rule.getInner().equals(param.getParaVal())){
                        String outerStatus = rule.getOuter();
                        //参数返回值是否产生告警
                        if(outerStatus.equals(SysConfigConstant.RPT_DEV_STATUS_ISALARM_YES)){
                            AlertInfo alertInfo = new AlertInfo().builder()
                                    .devType(respData.getDevType())
                                    .alertLevel(paraInfo.getAlertLevel())
                                    .devNo(respData.getDevNo())
                                    .alertTime(DateUtils.now())
                                    .ndpaNo(paraInfo.getParaNo())
                                    .alertDesc("").build();
                            DevAlertInfoContainer.addAlertInfo(alertInfo);
                        }
                    }
                });
            }
        });
    }
}
