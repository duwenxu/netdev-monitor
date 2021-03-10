package com.xy.netdev.rpt.service;


import com.xy.netdev.rpt.bo.RptBodyDev;

import java.util.List;

/**
 * <p>
 * 外部下发协议解析接口
 * </p>
 *
 * @author tangxl
 * @since 2021-03-08
 */
public interface IDownRptPrtclAnalysisService {
    /**
     * 外部下发协议
     * @return  下发的设备数据列表
     */
    List<RptBodyDev>  queryParaResponse();

}
