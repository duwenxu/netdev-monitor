package com.xy.netdev.rpt.service.impl;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.monitor.bo.FrameParaInfo;
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
        //设备型号(这里只需要获取子类型)
        int devTypeCode = ByteUtils.byteToNumber(dataBytes, 1, 1).intValue();
        //设备编号
        int devNo = ByteUtils.byteToNumber(dataBytes, 2, 1).byteValue();
        //设备参数数量
        int paramNum = ByteUtils.byteToNumber(dataBytes, 3, 1).intValue();
        int index = 4;
        RptBodyDev rptBodyDev = new RptBodyDev();
        rptBodyDev.setDevNo(String.valueOf(devNo));
        rptBodyDev.setDevParaTotal(String.valueOf(paramNum));
        rptBodyDev.setDevTypeCode(String.valueOf(devTypeCode));
        List<FrameParaData> devParaList = new ArrayList<>(paramNum);
        for (int i = 0; i <paramNum ; i++) {
            //数据体解析
            int paraNo = ByteUtils.byteToNumber(dataBytes, index, 1).intValue();
            index+=1;
            int devParamLen = ByteUtils.byteToNumber(dataBytes, index, 2).intValue();
            index+=2;
            FrameParaData frameParaData = new FrameParaData();
            frameParaData.setParaNo(String.valueOf(paraNo));
            String devType = BaseInfoContainer.getDevInfoByNo(String.valueOf(devNo)).getDevType();
            FrameParaInfo paraDetail = BaseInfoContainer.getParaInfoByNo(devType,String.valueOf(paraNo));
            String val = "";
            if(paraDetail.getDataType().equals(SysConfigConstant.PARA_DATA_TYPE_BYTE)){
                val = HexUtil.encodeHexStr(ByteUtils.byteArrayCopy(dataBytes, index, devParamLen));
            }else{
                val = new String(ByteUtils.byteArrayCopy(dataBytes, index, devParamLen));
                if(paraDetail.getTransOuttoInMap().size()>0){
                    val = paraDetail.getTransOuttoInMap().get(val);
                }
            }
            frameParaData.setParaVal(val);
            frameParaData.setDevNo(String.valueOf(devNo));
            frameParaData.setDevType(devType);
            frameParaData.setLen(devParamLen);
            index+=devParamLen;
            devParaList.add(frameParaData);
        }
        rptBodyDev.setDevParaList(devParaList);
        list.add(rptBodyDev);
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
