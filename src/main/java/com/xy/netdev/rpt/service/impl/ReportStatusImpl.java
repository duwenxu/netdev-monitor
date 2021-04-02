package com.xy.netdev.rpt.service.impl;

import at.favre.lib.bytes.Bytes;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.monitor.bo.DevStatusInfo;
import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.service.RequestService;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.placeholderByte;

/**
 * 状态上报
 * @author cc
 */
@Service
public class ReportStatusImpl implements RequestService {

    @Override
    @SuppressWarnings("unchecked")
    public byte[] pack(RptHeadDev rptHeadDev) {
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
            tempList.add(ByteUtils.objToBytes(devStatusInfo.getDevTypeCode(), 1));
            //设备编号
            tempList.add(ByteUtils.objToBytes(devStatusInfo.getDevNo(), 1));
            //设备状态
            String binaryStr =
                    //是否中断
                    devStatusInfo.getIsInterrupt() +
                    //是否告警
                    devStatusInfo.getIsAlarm() +
                    //是否启用主备
                    devStatusInfo.getIsUseStandby() +
                    //主用还是备用
                    devStatusInfo.getMasterOrSlave() +
                    //工作状态
                    devStatusInfo.getWorkStatus() +
                    "000";
            tempList.add(ByteUtils.objToBytes(Integer.parseInt(binaryStr, 2), 1));
        });
        return ByteUtils.listToBytes(tempList);
    }
}
