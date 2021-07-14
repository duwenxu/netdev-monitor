package com.xy.netdev.rpt.service;

import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.enums.StationCtlRequestEnums;

public interface RequestService{

    /**
     * 数据封包
     * @return 封包
     * @param rptHeadDev
     */
    byte[] pack(RptHeadDev rptHeadDev, StationCtlRequestEnums stationCtlRequestEnums);
}
