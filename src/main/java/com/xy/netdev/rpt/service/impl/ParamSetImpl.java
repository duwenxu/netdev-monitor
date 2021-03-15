package com.xy.netdev.rpt.service.impl;

import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.service.RequestService;
import com.xy.netdev.rpt.service.ResponseService;
import com.xy.netdev.rpt.service.StationControlHandler;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 参数设置命令
 * @author cc
 */
@Service
public class ParamSetImpl implements RequestService, ResponseService {


    @Override
    public List<RptBodyDev> unpackBody(StationControlHandler.StationControlHeadEntity stationControlHeadEntity) {

        return null;
    }

    private List<RptBodyDev> paramBuilder(byte[] dataBytes, List<RptBodyDev> list){
        if (dataBytes.length == 0){
            return list;
        }
        //设备型号
        int devTypeCode = ByteUtils.byteToNumber(dataBytes, 0, 2).intValue();
        //参数编号
        int devNo = ByteUtils.byteToNumber(dataBytes, 2, 1).byteValue();

        return list;
    }

    @Override
    public void callback(RptHeadDev headDev) {

    }

    @Override
    public byte[] pack(RptHeadDev rptHeadDev) {
        return new byte[0];
    }
}
