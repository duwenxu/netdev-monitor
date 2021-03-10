package com.xy.netdev.frame.service.impl;

import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.service.IBaseInfoService;
import com.xy.netdev.monitor.service.IParaInfoService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 39所Ku&L下变频器参数协议解析
 *
 * @author admin
 * @date 2021-03-05
 */
public class FreqConverterPrtcParser implements IParaPrtclAnalysisService {


    /**用户命令起始标记*/
    public final static String SEND_START_MARK = "<";
    /**设备响应开始标记*/
    public final static String RESP_START_MARK = ">";


    @Autowired
    IParaInfoService paraInfoService;
    @Autowired
    IBaseInfoService baseInfoService;


    @Override

    public void queryPara(BaseInfo devInfo, FrameParaInfo paraInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(SEND_START_MARK).append(devInfo.getDevIpAddr()).append("/")
                .append(paraInfo.getCmdMark());
        String command = sb.toString();
    }

    @Override

    public FrameParaInfo queryParaResponse(BaseInfo devInfo, FrameParaInfo paraInfo,byte[] dataByte) {
        String respStr = new String(dataByte);
        int beginIdx = respStr.indexOf("/");
        int endIdx = respStr.indexOf("_");
        String cmdMark = respStr.substring(beginIdx+1,endIdx);
        String value = respStr.substring(endIdx+1,respStr.indexOf("\\r"));
        paraInfo.setCmdMark(cmdMark);
        paraInfo.setParaVal(value);
        paraInfo.setDevNo(devInfo.getDevNo());
        paraInfo.setDevTypeCode(devInfo.getDevType());
        return paraInfo;
    }

    @Override
    public void ctrlPara(BaseInfo devInfo, FrameParaInfo paraInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(SEND_START_MARK).append(devInfo.getDevIpAddr()).append("/").append(paraInfo.getCmdMark())
                .append("_").append(paraInfo.getParaVal());
        String command = sb.toString();
    }

    @Override
    public FrameParaInfo ctrlParaResponse(BaseInfo devInfo, FrameParaInfo paraInfo,byte[] dataByte) {
        String respStr = new String(dataByte);
        int beginIdx = respStr.indexOf("/");
        int endIdx = respStr.indexOf("_");
        String cmdMark = respStr.substring(beginIdx+1,endIdx);
        String value = respStr.substring(endIdx+1,respStr.indexOf("\\r"));
        paraInfo.setCmdMark(cmdMark);
        paraInfo.setParaVal(value);
        paraInfo.setDevNo(devInfo.getDevNo());
        paraInfo.setDevTypeCode(devInfo.getDevType());
        return paraInfo;
    }
}
