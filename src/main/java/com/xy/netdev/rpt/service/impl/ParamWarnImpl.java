package com.xy.netdev.rpt.service.impl;

import com.alibaba.fastjson.JSON;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.monitor.entity.AlertInfo;
import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.service.RequestService;
import com.xy.netdev.rpt.service.ResponseService;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.xy.netdev.rpt.service.StationControlHandler.*;

/**
 * 参数警告命令
 * @author cc
 */
@Service
public class ParamWarnImpl implements RequestService, ResponseService {

    @Override
    public RptHeadDev unpackBody(StationControlHeadEntity stationControlHeadEntity, RptHeadDev headDev) {
        return unpackCommonHead(stationControlHeadEntity, headDev, bytes ->  paramBuilder(bytes, new ArrayList<>()));
    }


    private List<RptBodyDev> paramBuilder(byte[] dataBytes, List<RptBodyDev> list) {
        if (dataBytes == null || dataBytes.length == 0){
            return list;
        }
        //设备型号
        int devTypeCode = ByteUtils.byteToNumber(dataBytes, 0, 2).intValue();
        //参数编号
        int devNo = ByteUtils.byteToNumber(dataBytes, 2, 1).byteValue();
        //设备参数数量
        int paramNum = ByteUtils.byteToNumber(dataBytes, 3, 1).intValue();

        //数据体解析
        RptBodyDev rptBodyDev = new RptBodyDev();
        rptBodyDev.setDevNo(String.valueOf(devNo));
        rptBodyDev.setDevParaTotal(String.valueOf(paramNum));
        rptBodyDev.setDevTypeCode(String.valueOf(devTypeCode));
        //参数都是一字节, 数量==长度
        byte[] paramBytes = ByteUtils.byteArrayCopy(dataBytes, 4, paramNum);
        int index = getIndex(list, rptBodyDev, Objects.requireNonNull(paramBytes), paramBytes.length, 4);
        return paramBuilder(ByteUtils.byteArrayCopy(dataBytes, index, dataBytes.length - index), list);
    }


    @Override
    @SuppressWarnings("unchecked")
    public byte[] pack(RptHeadDev rptHeadDev) {
        List<AlertInfo> alertInfos = (List<AlertInfo>) rptHeadDev.getParam();
        List<byte[]> tempList = new ArrayList<>();
        //设置头
        setQueryResponseHead(rptHeadDev, tempList);
        alertInfos.forEach(alertInfo -> {
            //描述
            byte[] alertDesc = alertInfo.getAlertDesc().getBytes(Charset.forName("GB2312"));
            //告警等级
            tempList.add(ByteUtils.objToBytes(alertInfo.getAlertLevel(), 4));
            //告警等级描述长度
            tempList.add(ByteUtils.objToBytes(alertDesc.length, 1));
            //告警等级描述
            tempList.add(alertDesc);

        });
        return ByteUtils.listToBytes(tempList);
    }
}
