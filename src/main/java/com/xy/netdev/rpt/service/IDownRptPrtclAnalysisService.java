package com.xy.netdev.rpt.service;


import com.xy.netdev.rpt.bo.RptBodyDev;
import com.xy.netdev.rpt.bo.RptHeadDev;

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
     * 查询新的缓存状态
     * @param rptHeadDev
     * @return
     */
    RptHeadDev queryNewCache(RptHeadDev rptHeadDev);

    /**
     * 执行设备查询/设置命令
      * @param headDev
     */
    void doAction(RptHeadDev headDev);

}
