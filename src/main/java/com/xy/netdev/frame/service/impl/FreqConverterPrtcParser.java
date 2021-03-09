package com.xy.netdev.frame.service.impl;

import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.ParaInfo;


/**
 * 39所Ku&L下变频器参数协议解析
 *
 * @author admin
 * @date 2021-03-05
 */
public class FreqConverterPrtcParser implements IParaPrtclAnalysisService {


    /**用户命令起始标记*/
    private final static String SEND_START_MARK = "<";
    /**设备响应开始标记*/
    private final static String RESP_START_MARK = ">";
    /**用户命令结尾标记*/
    private final static String SEND_END_MARK = "'cr'";
    /**设备响应结尾标记*/
    private final static String RESP_END_MARK = "'cr''lf']";



    @Override
    public void queryPara(BaseInfo devInfo, ParaInfo paraInfo) {

    }

    @Override
    public ParaInfo queryParaResponse(BaseInfo devInfo, ParaInfo paraInfo) {
        return null;
    }

    @Override
    public void ctrlPara(BaseInfo devInfo, ParaInfo paraInfo) {

    }

    @Override
    public ParaInfo ctrlParaResponse(BaseInfo devInfo, ParaInfo paraInfo) {
        return null;
    }
}
