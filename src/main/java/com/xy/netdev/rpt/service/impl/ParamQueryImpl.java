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

/**
 * 参数查询命令
 * @author cc
 */
@Service
public class ParamQueryImpl implements RequestService, ResponseService {


    @Override
    public RptHeadDev unpackBody(StationControlHandler.StationControlHeadEntity stationControlHeadEntity) {
        return unpack(stationControlHeadEntity);
    }

    private RptHeadDev unpack(StationControlHandler.StationControlHeadEntity stationControlHeadEntity) {
        return unpackCommonHead(stationControlHeadEntity, bytes ->  paramBuilder(bytes, new ArrayList<>()));
    }


    private List<RptBodyDev> paramBuilder(byte[] dataBytes, List<RptBodyDev> list) {
        if (dataBytes.length == 0){
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
        byte[] paramBytes = ByteUtils.byteArrayCopy(dataBytes, 4, paramNum);
        int length = paramBytes.length;
        
        //参数解析
        List<FrameParaData> devParaList = new ArrayList<>(length);
        for (byte paramByte : paramBytes) {
            FrameParaData frameParaData = new FrameParaData();
            frameParaData.setParaNo(String.valueOf(paramByte));
            devParaList.add(frameParaData);
        }
        rptBodyDev.setDevParaList(devParaList);
        list.add(rptBodyDev);
        int index = length + 4;
        return paramBuilder(ByteUtils.byteArrayCopy(dataBytes, index, dataBytes.length - index), list);
    }

    @Override
    @SuppressWarnings("unchecked")
    public byte[] pack(RptHeadDev rptHeadDev) {

        List<RptBodyDev> rptBodyDevs = (List<RptBodyDev>) rptHeadDev.getParam();
        List<byte[]> tempList = new ArrayList<>();
        queryHead(rptHeadDev, tempList);

        rptBodyDevs.forEach(rptBodyDev -> {
            queryHeadNext(tempList, rptBodyDev);
            List<FrameParaData> devParaList = rptBodyDev.getDevParaList();
            int parmaSize = devParaList.size();
            //设备参数数量
            tempList.add(ByteUtils.objToBytes(parmaSize, 1));
            devParaList.forEach(frameParaData -> {
                //参数编号
                tempList.add(ByteUtils.objToBytes(frameParaData.getParaNo(), 1));
                //数据长度
                tempList.add(ByteUtils.objToBytes(frameParaData.getLen(), 2));
                //数据体内容
                tempList.add(ByteUtils.objToBytes(frameParaData.getParaVal(), frameParaData.getLen()));
            });
        });
        return listToBytes(tempList);
    }
}
