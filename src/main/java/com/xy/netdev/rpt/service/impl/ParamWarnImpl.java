package com.xy.netdev.rpt.service.impl;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.monitor.entity.AlertInfo;
import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.enums.StationCtlRequestEnums;
import com.xy.netdev.rpt.service.RequestService;
import com.xy.netdev.rpt.service.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.*;

import static com.xy.netdev.rpt.service.StationControlHandler.*;

/**
 * 参数警告命令
 * @author cc
 */
@Service
public class ParamWarnImpl implements RequestService, ResponseService {

    @Autowired
    private ParamQueryImpl paramQuery;
    @Autowired
    private ISysParamService sysParamService;

    @Override
    public RptHeadDev unpackBody(StationControlHeadEntity stationControlHeadEntity, RptHeadDev headDev) {
        return unpackCommonHead(stationControlHeadEntity, headDev, bytes ->  paramBuilder(bytes, new ArrayList<>()));
    }


    private List<RptBodyDev> paramBuilder(byte[] dataBytes, List<RptBodyDev> list) {
        if (dataBytes == null || dataBytes.length == 0){
            return list;
        }
        //设备型号
        int devTypeCode = ByteUtils.byteToNumber(dataBytes, 1, 1).intValue();
        //设备编号
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
    public byte[] pack(RptHeadDev rptHeadDev,StationCtlRequestEnums stationCtlRequestEnums) {
        List<AlertInfo> alertInfos = (List<AlertInfo>) rptHeadDev.getParam();
        List<byte[]> tempList = new ArrayList<>();
        Map<String,List<AlertInfo>> devAlertMap = new HashMap<>();
        for (AlertInfo alertInfo : alertInfos) {
            if(devAlertMap.containsKey(alertInfo.getDevNo())){
                devAlertMap.get(alertInfo.getDevNo()).add(alertInfo);
            }else{
                List<AlertInfo> alertInfoList = new ArrayList<>();
                alertInfoList.add(alertInfo);
                devAlertMap.put(alertInfo.getDevNo(),alertInfoList);
            }
        }
        rptHeadDev.setDevNum(devAlertMap.size());
        //设置头
        setQueryResponseHead(rptHeadDev, tempList);
        for (List<AlertInfo> value : devAlertMap.values()) {
            byte[] devType= new byte[2];
            devType[0] = 0x39;
            devType[1] = (byte) Integer.parseInt(sysParamService.getParaRemark1(value.get(0).getDevType()),16);
            //设备型号
            tempList.add(devType);
            //设备编号
            tempList.add(ByteUtils.objToBytes(value.get(0).getDevNo(), 1));
            //参数数量
            tempList.add(ByteUtils.objToBytes(value.size(), 1));
            for (AlertInfo alertInfo : value) {
                //参数编号
                tempList.add(ByteUtils.objToBytes(alertInfo.getNdpaNo(), 1));
                //告警等级
                tempList.add(ByteUtils.objToBytes(Integer.parseInt(sysParamService.getParaRemark1(alertInfo.getAlertLevel())), 4));
                //告警等级描述
                byte[] alertLevelDesc = sysParamService.getParaName(alertInfo.getAlertLevel()).getBytes(Charset.forName("GB2312"));
                //告警等级描述长度
                tempList.add(ByteUtils.objToBytes(alertLevelDesc.length, 1));
                //告警等级描述
                tempList.add(alertLevelDesc);
            }
        }
        return paramQuery.packHeadBytes(tempList, StationCtlRequestEnums.PARA_WARNING_QUERY_RESP);
    }
}
