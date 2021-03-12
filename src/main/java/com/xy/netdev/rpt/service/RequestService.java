package com.xy.netdev.rpt.service;

import com.xy.netdev.rpt.bo.RptHeadDev;

public interface RequestService{

    /**
     * 数据封包
     * @return 封包
     * @param rptHeadDev
     */
    byte[] pack(RptHeadDev rptHeadDev);
}
