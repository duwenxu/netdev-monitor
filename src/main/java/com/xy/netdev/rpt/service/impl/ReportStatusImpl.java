package com.xy.netdev.rpt.service.impl;

import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.service.RequestService;
import org.springframework.stereotype.Service;

/**
 * 状态上报
 * @author cc
 */
@Service
public class ReportStatusImpl implements RequestService {
    @Override
    public byte[] pack(RptHeadDev rptHeadDev) {
        return new byte[0];
    }
}
