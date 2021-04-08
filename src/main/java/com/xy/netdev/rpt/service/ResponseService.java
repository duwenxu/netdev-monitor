package com.xy.netdev.rpt.service;

import com.xy.netdev.rpt.bo.RptHeadDev;

public interface ResponseService {
    /**
     * 数据解析数据体
     * @param stationControlHeadEntity 站控对象
     * @param headDev
     * @return 中心数据格式
     */
    RptHeadDev unpackBody(StationControlHandler.StationControlHeadEntity stationControlHeadEntity, RptHeadDev headDev);

}
