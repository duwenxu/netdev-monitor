package com.xy.netdev.rpt.service;

import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.bo.RptHeadDev;

import java.util.List;

public interface ResponseService {
    /**
     * 数据解析数据体
     * @param stationControlHeadEntity 站控对象
     * @return 中心数据格式
     */
    List<RptBodyDev> unpackBody(StationControlHandler.StationControlHeadEntity stationControlHeadEntity);

    /**
     * 调用系统方法
     * @param list 参数
     */
    void answer(RptHeadDev headDev);
}
