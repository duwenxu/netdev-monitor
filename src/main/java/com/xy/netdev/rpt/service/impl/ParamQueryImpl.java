package com.xy.netdev.rpt.service.impl;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.monitor.constant.MonitorConstants;
import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.enums.StationCtlRequestEnums;
import com.xy.netdev.rpt.service.RequestService;
import com.xy.netdev.rpt.service.ResponseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.xy.netdev.common.util.ByteUtils.listToBytes;
import static com.xy.netdev.common.util.ByteUtils.placeholderByte;
import static com.xy.netdev.rpt.service.StationControlHandler.*;

/**
 * 参数查询命令
 * @author cc
 */
@Service
@Slf4j
public class ParamQueryImpl implements RequestService, ResponseService {

    @Autowired
    private ISysParamService iSysParamService;

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
    public byte[] pack(RptHeadDev rptHeadDev,StationCtlRequestEnums stationCtlRequestEnums) {
        List<byte[]> dataBytes = commonPack(rptHeadDev, (devParaList, tempList) -> {
            devParaList.forEach(frameParaData -> {
                if (frameParaData.getLen() == null) {
                    log.warn("参数查询命令生成失败:{}", JSON.toJSONString(frameParaData));
                }
                if (StrUtil.isNotBlank(frameParaData.getParaVal())) {
                    String paraDatatype = DevParaInfoContainer.getDevParaView(frameParaData.getDevNo(), frameParaData.getParaNo()).getParaDatatype();
                    byte[] bytes = frameParaData.getParaVal().getBytes(Charset.forName("GB2312"));
                    if (MonitorConstants.BYTE.equals(paraDatatype)){
                        try {
                            bytes = HexUtil.decodeHex(frameParaData.getParaVal());
                            ByteUtils.objToBytes(frameParaData.getParaVal(),frameParaData.getLen());
                        } catch (Exception e) {
                            log.warn("上报信息数据体字节转换异常：需要转换的参数值：[{}]",frameParaData.getParaVal());
                        }
                    }
                    //参数编号
                    tempList.add(ByteUtils.objToBytes(frameParaData.getParaNo(), 1));
                    //数据长度
                    tempList.add(ByteUtils.objToBytes(bytes.length, 2));
                    //数据体内容
                    tempList.add(bytes);
//                    tempList.add( ByteUtils.objToBytes(frameParaData.getParaVal(), frameParaData.getLen()));
                }
            });
        });
        byte[] bytes = packHeadBytes(dataBytes, stationCtlRequestEnums);
        return bytes;
    }

    public byte[] packHeadBytes(List<byte[]> dataBytes,StationCtlRequestEnums stationCtlRequestEnums) {
        byte[] data = listToBytes(dataBytes);
        //数据字段长度
        byte[] dataLen = ByteUtils.objToBytes(data.length, 2);
        //数据头 预留字段
        byte[] headPlaceHolder = placeholderByte(4);
        //信息类别
        byte[] type = ByteUtils.objToBytes(Integer.parseInt(stationCtlRequestEnums.getCmdCode()), 2);
        List<byte[]> headBytes = Arrays.asList(type, dataLen, headPlaceHolder);
        return ByteUtils.bytesMerge(listToBytes(headBytes), data);
    }


}
