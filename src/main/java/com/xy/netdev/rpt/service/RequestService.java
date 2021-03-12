package com.xy.netdev.rpt.service;

import com.xy.netdev.rpt.bo.RptBodyDev;

import java.util.List;

public interface RequestService{

    /**
     * 数据封包
     * @return 封包
     */
    byte[] pack(Object obj);
}
