package com.xy.netdev.transit.util;

import com.xy.common.exception.BaseException;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.factory.ParaPrtclFactory;
import com.xy.netdev.factory.QueryInterPrtcllFactory;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.AlertInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.PrtclFormat;

import java.util.Map;

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
        throw new BaseException("设备编号"+frameReqData.getDevNo()+"标识符是"+frameReqData.getCmdMark()+"的接口未配置协议格式!");
    }

    /**
     * 获取参数服务
     * @param  frameReqData   协议解析请求数据
     */
    public static IQueryInterPrtclAnalysisService getQueryInterPrtclAnalysisService(FrameReqData frameReqData){
        PrtclFormat  prtclFormat =BaseInfoContainer.getPrtclByInterfaceOrPara(frameReqData.getDevType(),frameReqData.getCmdMark());
        if (prtclFormat != null) {
            return QueryInterPrtcllFactory.genHandler(prtclFormat.getFmtHandlerClass());
        }
        throw new BaseException("设备编号"+frameReqData.getDevNo()+"标识符是"+frameReqData.getCmdMark()+"的接口未配置协议格式!");
    }

    /**
     * 生成报警描述信息
     * @param  devInfo    设备信息
     * @param  paraInfo   参数信息
     */
    public static String genAlertDesc(BaseInfo devInfo, FrameParaInfo paraInfo){
        String alertStatusInfo = "error";
        Map<String,Object> selectMap = paraInfo.getSelectMap();
        if(selectMap!=null&&!selectMap.isEmpty()){
            if(selectMap.containsKey(paraInfo.getParaVal())){
                alertStatusInfo = ""+selectMap.get(paraInfo.getParaVal());
            }
        }
        return "设备:"+devInfo.getDevName()+"参数:"+paraInfo.getParaName()+"报警:"+alertStatusInfo;
    }
}
