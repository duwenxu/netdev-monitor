package com.xy.netdev.rpt.service.impl;

import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.service.RequestService;
import com.xy.netdev.rpt.service.ResponseService;
import com.xy.netdev.rpt.service.StationControlHandler;

import java.util.List;

/**
 * 参数警告命令
 * @author cc
 */
public class ParamWarnImpl implements RequestService, ResponseService {

    @Override
    public List<RptBodyDev> unpackBody(StationControlHandler.StationControlHeadEntity stationControlHeadEntity) {
        return null;
    }


    @Override
    public byte[] pack(RptHeadDev rptHeadDev) {
        return new byte[0];
    }
}
