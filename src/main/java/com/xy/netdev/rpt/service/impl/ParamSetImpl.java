package com.xy.netdev.rpt.service.impl;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.enums.StationCtlRequestEnums;
import com.xy.netdev.rpt.service.RequestService;
import com.xy.netdev.rpt.service.ResponseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xy.netdev.common.util.ByteUtils.listToBytes;
import static com.xy.netdev.common.util.ByteUtils.placeholderByte;
import static com.xy.netdev.rpt.service.StationControlHandler.*;

/**
 * 参数设置命令
 * @author cc
 */
@Service
@Slf4j
public class ParamSetImpl implements RequestService, ResponseService {

    @Autowired
    private ParamQueryImpl paramQuery;

    @Autowired
    private ISysParamService sysParamService;

    @Override
    public RptHeadDev unpackBody(StationControlHeadEntity stationControlHeadEntity, RptHeadDev headDev) {
        return unpackCommonHead(stationControlHeadEntity, headDev, bytes ->  paramBuilder(bytes, new ArrayList<>()));
    }


    private List<RptBodyDev> paramBuilder(byte[] dataBytes, List<RptBodyDev> list){
        if (dataBytes == null || dataBytes.length == 0){
            return list;
        }
        //设备型号
        int devTypeNo = Objects.requireNonNull(ByteUtils.byteArrayCopy(dataBytes, 0, 2))[1];
        String devType = null;
        try {
            devType = sysParamService.queryParamsByParentId("0020").stream().filter(param -> String.valueOf(devTypeNo).equals(param.getRemark1())).collect(Collectors.toList()).get(0).getParaCode();
        } catch (Exception e) {
            log.error("常量表remark1參數映射錯誤：remark1:{}",devTypeNo);
        }
        //設備编号
        int devNo = ByteUtils.byteToNumber(dataBytes, 2, 1).byteValue();
        //设备参数数量
        int paramNum = ByteUtils.byteToNumber(dataBytes, 3, 1).intValue();
        //參數編號
        int devParaNo = ByteUtils.byteToNumber(dataBytes, 4, 1).intValue();
        //參數長度
        int devParamLen = ByteUtils.byteToNumber(dataBytes, 5, 2).intValue();

        //数据体解析
        RptBodyDev rptBodyDev = new RptBodyDev();
        rptBodyDev.setDevNo(String.valueOf(devNo));
        rptBodyDev.setDevParaTotal(String.valueOf(paramNum));
        rptBodyDev.setDevTypeCode(String.valueOf(devType));
        rptBodyDev.setDevParamLen(devParamLen);
        byte[] paramBytes = ByteUtils.byteArrayCopy(dataBytes, 7, devParamLen);
        int index = getIndex(list, rptBodyDev, Objects.requireNonNull(paramBytes), paramBytes.length, 7);
        return paramBuilder(ByteUtils.byteArrayCopy(dataBytes, index, dataBytes.length - index), list);
    }


    @Override
    public byte[] pack(RptHeadDev rptHeadDev,StationCtlRequestEnums stationCtlRequestEnums) {
        List<byte[]> dataBytes = commonPack(rptHeadDev, (devParaList, tempList) -> {
            devParaList.forEach(frameParaData -> {
                //参数编号
                tempList.add(ByteUtils.objToBytes(frameParaData.getParaNo(), 1));
                //数据响应值
                tempList.add(ByteUtils.objToBytes(frameParaData.getParaSetRes(), 2));
            });
        });
        return paramQuery.packHeadBytes(dataBytes, StationCtlRequestEnums.PARA_SET_RESPONSE);
    }


}
