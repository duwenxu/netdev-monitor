package com.xy.netdev.transit.util;

import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.factory.ParaPrtclFactory;
import com.xy.netdev.factory.QueryInterPrtcllFactory;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.monitor.entity.PrtclFormat;

/**
 * <p>
 * 中转层 数据处理辅助类
 * </p>
 *
 * @author tangxl
 * @since 2021-03-11
 */
public class DataHandlerHelper {
    /**
     * 获取参数服务
     * @param  frameReqData   协议解析请求数据
     */
    public static IParaPrtclAnalysisService getParaPrtclAnalysisService(FrameReqData frameReqData){
        PrtclFormat  prtclFormat =BaseInfoContainer.getPrtclByPara(frameReqData.getDevType(),frameReqData.getCmdMark());
        if (prtclFormat != null) {
            return ParaPrtclFactory.genHandler(prtclFormat.getFmtHandlerClass());
        }
        return null;
    }

    /**
     * 获取参数服务
     * @param  frameReqData   协议解析请求数据
     */
    public static IQueryInterPrtclAnalysisService getQueryInterPrtclAnalysisService(FrameReqData frameReqData){
        PrtclFormat  prtclFormat =BaseInfoContainer.getPrtclByInterfaceOrPara(frameReqData.getDevType(),frameReqData.getCmdMark());
        return QueryInterPrtcllFactory.genHandler(prtclFormat.getFmtHandlerClass());
    }
}
