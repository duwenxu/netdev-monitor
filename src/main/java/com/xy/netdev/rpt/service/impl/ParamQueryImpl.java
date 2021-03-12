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
import static com.xy.netdev.common.util.ByteUtils.placeholderByte;

/**
 * 参数查询命令
 * @author cc
 */
@Service
public class ParamQueryImpl implements RequestService, ResponseService {


    @Override
    public RptHeadDev unpackBody(StationControlHandler.StationControlHeadEntity stationControlHeadEntity) {
        byte[] paramData = stationControlHeadEntity.getParamData();
        //查询标识
        int cmdMark = ByteUtils.byteToNumber(paramData, 4, 1).intValue();
        //站号
        int stationNo = ByteUtils.byteToNumber(paramData, 5, 1).intValue();
        //设备数量
        int devNum = ByteUtils.byteToNumber(paramData, 6, 1).intValue();
        //参数
        byte[] dataBytes = ByteUtils.byteArrayCopy(paramData, 7, paramData.length - 7);

        List<RptBodyDev> rptBodyDevs = paramBuilder(dataBytes, new ArrayList<>(devNum));

        RptHeadDev rptHeadDev = new RptHeadDev();
        rptHeadDev.setStationNo(String.valueOf(stationNo));
        rptHeadDev.setCmdMarkHexStr(Integer.toHexString(cmdMark));
        rptHeadDev.setParam(rptBodyDevs);
        rptHeadDev.setDevNo(stationControlHeadEntity.getBaseInfo().getDevNo());
        return rptHeadDev;
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
    public void callback(RptHeadDev headDev) {

    }

    @Override
    @SuppressWarnings("unchecked")
    public byte[] pack(RptHeadDev rptHeadDev) {

        List<RptBodyDev> rptBodyDevs = (List<RptBodyDev>) rptHeadDev.getParam();
        List<byte[]> tempList = new ArrayList<>();
        //保留
        tempList.add(placeholderByte(4));
        //查询标志
        tempList.add(ByteUtils.NumToBytes(Integer.parseInt(rptHeadDev.getCmdMarkHexStr(), 16), 1));
        //站号
        tempList.add(ByteUtils.NumToBytes(Integer.parseInt(rptHeadDev.getStationNo()), 1));
        return listToBytes(tempList);
    }

}
