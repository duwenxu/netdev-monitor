package com.xy.netdev.rpt.service.impl;

import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.service.RequestService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 状态上报
 * @author cc
 */
@Service
public class ReportStatusImpl implements RequestService {
    @Override
    public byte[] pack(Object obj) {
        return new byte[0];
    }
}
