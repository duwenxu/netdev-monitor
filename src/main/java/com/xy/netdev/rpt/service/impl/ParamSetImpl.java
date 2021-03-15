package com.xy.netdev.rpt.service.impl;

import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.service.RequestService;
import com.xy.netdev.rpt.service.ResponseService;
import com.xy.netdev.rpt.service.StationControlHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.listToBytes;
import static com.xy.netdev.rpt.service.StationControlHandler.*;
import static com.xy.netdev.rpt.service.StationControlHandler.queryHeadNext;

/**
 * 参数设置命令
 * @author cc
 */
@Service
public class ParamSetImpl implements RequestService, ResponseService {

    @Override
    public RptHeadDev unpackBody(StationControlHandler.StationControlHeadEntity stationControlHeadEntity) {
        return unpackCommonHead(stationControlHeadEntity, bytes ->  paramBuilder(bytes, new ArrayList<>()));
    }


    private List<RptBodyDev> paramBuilder(byte[] dataBytes, List<RptBodyDev> list){
        if (dataBytes.length == 0){
            return list;
        }
        //设备型号
        int devTypeCode = ByteUtils.byteToNumber(dataBytes, 0, 2).intValue();
        //参数编号
        int devNo = ByteUtils.byteToNumber(dataBytes, 2, 1).byteValue();
        //设备参数数量
        int paramNum = ByteUtils.byteToNumber(dataBytes, 3, 1).intValue();
        //设备数据长度
        int devParamLen = ByteUtils.byteToNumber(dataBytes, 4, 2).intValue();

        //数据体解析
        RptBodyDev rptBodyDev = new RptBodyDev();
        rptBodyDev.setDevNo(String.valueOf(devNo));
        rptBodyDev.setDevParaTotal(String.valueOf(paramNum));
        rptBodyDev.setDevTypeCode(String.valueOf(devTypeCode));
        rptBodyDev.setDevParamLen(devParamLen);

        byte[] paramBytes = ByteUtils.byteArrayCopy(dataBytes, 6, devParamLen);

        int index = getIndex(list, rptBodyDev, paramBytes, paramBytes.length, 6);
        return paramBuilder(ByteUtils.byteArrayCopy(dataBytes, index, dataBytes.length - index), list);
    }


    @Override
    public byte[] pack(RptHeadDev rptHeadDev) {
        return commonPack(rptHeadDev, (devParaList, tempList) -> {
            devParaList.forEach(frameParaData -> {
                //参数编号
                tempList.add(ByteUtils.objToBytes(frameParaData.getParaNo(), 1));
                //数据响应值
                tempList.add(ByteUtils.objToBytes(frameParaData.getParaVal(), 2));
            });
        });
    }


}