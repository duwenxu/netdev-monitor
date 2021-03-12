package com.xy.netdev.rpt.service.impl;

import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.service.RequestService;

import java.util.List;

/**
 * 告警时间上报
 * @author cc
 */
public class ReportWarnImpl implements RequestService {
    @Override
    public byte[] pack(List<RptBodyDev> list) {
        return new byte[0];
    }
}
