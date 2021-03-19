package com.xy.netdev.rpt.service;

public interface ResponseService {
    /**
     * 数据解析数据体
     * @param stationControlHeadEntity 站控对象
     * @return 中心数据格式
     */
    Object unpackBody(StationControlHandler.StationControlHeadEntity stationControlHeadEntity);

}
