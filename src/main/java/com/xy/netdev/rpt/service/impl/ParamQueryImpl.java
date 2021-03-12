package com.xy.netdev.rpt.service.impl;

import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.service.RequestService;
import com.xy.netdev.rpt.service.ResponseService;
import com.xy.netdev.rpt.service.StationControlHandler;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 参数查询命令
 * @author cc
 */
@Service
public class ParamQueryImpl implements RequestService, ResponseService {


    @Override
    public byte[] pack(List<RptBodyDev> list) {
        return new byte[0];
    }

    @Override
    public List<RptBodyDev> unpackBody(StationControlHandler.StationControlHeadEntity stationControlHeadEntity) {
        return null;
    }

    @Override
    public void answer(List<RptBodyDev> list) {

    }
}
