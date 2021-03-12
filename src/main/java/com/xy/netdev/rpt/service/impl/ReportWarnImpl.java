package com.xy.netdev.rpt.service.impl;

import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.service.RequestService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.*;

/**
 * 告警时间上报
 * @author cc
 */
public class ReportWarnImpl implements RequestService {

    public static void main(String[] args) {
        System.out.println(Arrays.toString(placeholderByte(5)));
    }

    @Override
    public byte[] pack(RptHeadDev rptHeadDev) {
        List<byte[]> tempList = new ArrayList<>();
        //保留
        tempList.add(placeholderByte(5));
        //站号
//        tempList.add(objectToBytes(list.get(0).getStationNo(), 1));
//        //设备数量
//        tempList.add(objectToBytes(list.size(), 1));
//        list.forEach(rptBodyDev -> {
//
//        });
        return listToBytes(tempList);
    }
}
