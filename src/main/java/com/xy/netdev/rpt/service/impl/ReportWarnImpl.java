package com.xy.netdev.rpt.service.impl;

import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.monitor.entity.AlertInfo;
import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.enums.StationCtlRequestEnums;
import com.xy.netdev.rpt.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.listToBytes;
import static com.xy.netdev.common.util.ByteUtils.placeholderByte;

/**
 * 告警时间上报
 * @author cc
 */
@Service
public class ReportWarnImpl implements RequestService {

    @Autowired
    private ParamQueryImpl paramQuery;

    @Override
    @SuppressWarnings("unchecked")
    public byte[] pack(RptHeadDev rptHeadDev,StationCtlRequestEnums stationCtlRequestEnums) {
        AlertInfo alertInfo = (AlertInfo) rptHeadDev.getParam();
        List<byte[]> tempList = new ArrayList<>();
        //保留
        tempList.add(placeholderByte(6));
        //告警个数
        tempList.add(ByteUtils.objToBytes(alertInfo.getAlertNum(), 1));
        //告警时间
        tempList.add(ByteUtils.objToBytes(alertInfo.getAlertTime(), 20));
        //站号
        tempList.add(ByteUtils.objToBytes(alertInfo.getAlertStationNo(), 1));
        //设备型号
        tempList.add(ByteUtils.objToBytes(alertInfo.getDevType(), 2));
        //设备编号
        tempList.add(ByteUtils.objToBytes(alertInfo.getDevNo(), 1));
        //参数编号
        tempList.add(ByteUtils.objToBytes(alertInfo.getNdpaNo(), 1));
        //告警级别
        tempList.add(ByteUtils.objToBytes(alertInfo.getAlertLevel(), 4));
        //描述
        byte[] alertDesc = alertInfo.getAlertDesc().getBytes(Charset.forName("GB2312"));
        //告警描述长度
        tempList.add(ByteUtils.objToBytes(alertDesc.length, 1));
        //告警描述
        tempList.add(alertDesc);
        return paramQuery.packHeadBytes(tempList, StationCtlRequestEnums.PARA_WARNING_QUERY_RESP);
    }
}
