package com.xy.netdev.transit.impl;

import com.alibaba.fastjson.JSONArray;
import com.xy.common.util.DateUtils;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevAlertInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.monitor.bo.DevStatusInfo;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.bo.TransRule;
import com.xy.netdev.monitor.entity.AlertInfo;
import com.xy.netdev.transit.IDevInfoReportService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 设备状态、告警事件上报
 *
 * @author luo
 * @date 2021-03-11
 */
@Service
public class DevInfoReportServiceImpl implements IDevInfoReportService {

    /**
     * 告警标识
     */
    public static final String ERROR = "1";


    @Override
    public void generateStatusInfo(FrameRespData respData) {
        List<FrameParaData> params =  respData.getFrameParaList();
        DevStatusInfo statusInfo = new DevStatusInfo();
        //statusInfo.set
    }

    /**
     * 生成告警信息
     * @param respData
     * @return
     */
    @Override
    public void generateAlarmInfo(FrameRespData respData) {
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
                        if(outerStatus.equals(ERROR)){
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
