package com.xy.netdev.transit.impl;

import com.alibaba.fastjson.JSONArray;
import com.xy.common.util.DateUtils;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevAlertInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.bo.ParaSpinnerInfo;
import com.xy.netdev.monitor.bo.TransRule;
import com.xy.netdev.monitor.entity.AlertInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.transit.IDevAlarmReportService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 设备告警事件上报
 *
 * @author luo
 * @date 2021-03-11
 */
@Service
public class DevAlarmReportServiceImpl implements IDevAlarmReportService {

    /**
     * 告警标识
     */
    public static final String ERROR = "1";

    /**
     * 生成告警信息
     * @param respData
     * @return
     */
    @Override
    public void generateAlarmInfo(FrameRespData respData) {
        List<FrameParaData> params =  respData.getFrameParaList();
        for (FrameParaData param : params) {
            FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByCmd(param.getDevType(),param.getParaNo());
            String ruleStr =  paraInfo.getTransRule();
            //读取参数配置的状态转换规则
            List<TransRule> rules = JSONArray.parseArray(ruleStr, TransRule.class);
            String status = paraInfo.getAlertPara();
            if(status.equals(SysConfigConstant.DEV_STATUS_ALARM)){
                for (TransRule rule : rules) {
                    if(rule.getInner().equals(param.getParaVal())){
                        String outerStatus = rule.getOuter();
                        //参数返回值是否产生告警
                        if(outerStatus.equals(ERROR)){
                            AlertInfo alertInfo = new AlertInfo();
                            alertInfo.setDevType(respData.getDevType());
                            alertInfo.setAlertLevel(paraInfo.getAlertLevel());
                            alertInfo.setDevNo(respData.getDevNo());
                            alertInfo.setAlertTime(DateUtils.now());
                            alertInfo.setNdpaNo(paraInfo.getParaNo());
                            alertInfo.setAlertDesc("");
                            DevAlertInfoContainer.addAlertInfo(alertInfo);
                        }
                    }
                }
            }
        }
    }


}
