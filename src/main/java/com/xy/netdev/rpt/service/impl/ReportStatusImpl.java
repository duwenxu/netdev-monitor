package com.xy.netdev.rpt.service.impl;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.monitor.bo.DevStatusInfo;
import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.enums.StationCtlRequestEnums;
import com.xy.netdev.rpt.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.placeholderByte;

/**
 * 状态上报
 * @author cc
 */
@Service
public class ReportStatusImpl implements RequestService {

    @Autowired
    private ParamQueryImpl paramQuery;
    @Autowired
    private ISysParamService sysParamService;

    @Override
    @SuppressWarnings("unchecked")
    public byte[] pack(RptHeadDev rptHeadDev,StationCtlRequestEnums stationCtlRequestEnums) {
        List<byte[]> tempList = new ArrayList<>();
        //保留
        tempList.add(placeholderByte(5));
        //站号
        tempList.add(ByteUtils.objToBytes(rptHeadDev.getStationNo(), 1));
        //设备数量
        tempList.add(ByteUtils.objToBytes(rptHeadDev.getDevNum(), 1));
        //设备状态信息
        List<DevStatusInfo> devStatusInfoList = (List<DevStatusInfo>) rptHeadDev.getParam();
        devStatusInfoList.forEach(devStatusInfo -> {
            //设备型号
            byte[] devType= new byte[2];
            devType[0] = 0x39;
            String devSubType = devStatusInfo.getDevTypeCode();
            if(devSubType.length()==7 && devSubType.startsWith("0020")){
                devSubType = sysParamService.getParaRemark1(devStatusInfo.getDevTypeCode());
            }
            devType[1] = (byte) Integer.parseInt(devSubType,16);
            tempList.add(devType);
            //设备编号
            tempList.add(ByteUtils.objToBytes(devStatusInfo.getDevNo(), 1));
            //设备状态
            String binaryStr = "000" +
                    //工作状态
                    devStatusInfo.getWorkStatus() +
                    //主用还是备用
                    devStatusInfo.getMasterOrSlave() +
                    //是否启用主备
                    devStatusInfo.getIsUseStandby() +
                    //是否告警
                    devStatusInfo.getIsAlarm() +
                    //是否中断
                    devStatusInfo.getIsInterrupt();
            tempList.add(ByteUtils.objToBytes(Integer.parseInt(binaryStr, 2), 1));
        });
        return paramQuery.packHeadBytes(tempList, StationCtlRequestEnums.DEV_STATUS_REPORT);
    }
}
