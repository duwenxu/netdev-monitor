package com.xy.netdev.rpt.service;


import com.xy.netdev.rpt.bo.RptBodyDev;

import java.util.List;

/**
 * <p>
 * 上报外部协议解析接口
 * </p>
 *
 * @author tangxl
 * @since 2021-03-08
 */
public interface IUpRptPrtclAnalysisService {
    /**
     * 上报外部协议
     * @param  paraList  上报的设备数据列表
     */
    void queryParaResponse(List<RptBodyDev> paraList);

}
