package com.xy.netdev.transit.impl;

import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.AlertInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.transit.IDevAlarmReportService;

import java.util.List;

/**
 * 设备告警事件上报
 *
 * @author luo
 * @date 2021-03-11
 */
public class DevAlarmReportServiceImpl implements IDevAlarmReportService {

    @Override
    public AlertInfo generateAlarmInfo(FrameRespData respData) {
        String cmdMark = respData.getCmdMark();
        List<FrameParaData> params =  respData.getFrameParaList();
        int alarmNum = 0;
        AlertInfo alertInfo = new AlertInfo();
        params.forEach(param -> {
            FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByCmd(param.getDevType(),param.getParaNo());
            String status = paraInfo.getAlertPara();
            if(status.equals(SysConfigConstant))

        });


        return null;
    }

    @Override
    public void eventReport(AlertInfo alertInfo) {

    }
}
