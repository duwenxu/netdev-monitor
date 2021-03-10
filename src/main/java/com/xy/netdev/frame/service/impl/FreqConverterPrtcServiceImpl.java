package com.xy.netdev.frame.service.impl;

import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.entity.TransportEntity;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.monitor.service.IBaseInfoService;
import com.xy.netdev.monitor.service.IParaInfoService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;


/**
 * 39所Ku&L下变频器参数协议解析
 *
 * @author admin
 * @date 2021-03-05
 */
public class FreqConverterPrtcServiceImpl implements IParaPrtclAnalysisService {


    /**用户命令起始标记*/
    public final static String SEND_START_MARK = "<";
    /**设备响应开始标记*/
    public final static String RESP_START_MARK = ">";


    @Autowired
    SocketMutualService socketMutualService;


    @Override
    public void queryPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(SEND_START_MARK).append(reqInfo.getDevNo()).append("/")
                .append(reqInfo.getCmdMark());
        String command = sb.toString();
        TransportEntity transportEntity = new TransportEntity();
        BaseInfo baseInfo = null;
        transportEntity.setDevInfo(baseInfo);
        transportEntity.setParamMark(reqInfo.getCmdMark());
        transportEntity.setParamBytes(command.getBytes());
        socketMutualService.request(transportEntity, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(TransportEntity transportEntity) {
        String respStr = new String(transportEntity.getParamBytes());
        int beginIdx = respStr.indexOf("/");
        int endIdx = respStr.indexOf("_");
        String cmdMark = respStr.substring(beginIdx+1,endIdx);
        String value = respStr.substring(endIdx+1,respStr.indexOf("\\r"));
        FrameRespData respData = new FrameRespData();
        ParaInfo paraDetil = null;
        respData.setCmdMark(cmdMark);
        respData.setAccessType(paraDetil.getNdpaAccessRight());
        respData.setDevNo(transportEntity.getDevInfo().getDevNo());
        respData.setDevType(transportEntity.getDevInfo().getDevType());
        respData.setOperType(SysConfigConstant.OPREATE_QUERY_RESP);
        List<FrameParaData> frameParaDatas = new ArrayList<>();
        FrameParaData frameParaData = new FrameParaData();
        frameParaData.setParaNo(paraDetil.getNdpaNo());
        frameParaData.setParaVal(value);
        frameParaData.setDevType(transportEntity.getDevInfo().getDevType());
        frameParaData.setDevNo(transportEntity.getDevInfo().getDevNo());
        frameParaDatas.add(frameParaData);
        respData.setFrameParaList(frameParaDatas);
        return respData;
    }

    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(SEND_START_MARK).append(reqInfo.getDevNo()).append("/").append(reqInfo.getCmdMark())
                .append("_").append(reqInfo.getFrameParaList().get(0).getParaVal());
        String command = sb.toString();
        TransportEntity transportEntity = new TransportEntity();
        BaseInfo baseInfo = null;
        transportEntity.setDevInfo(baseInfo);
        transportEntity.setParamMark(reqInfo.getCmdMark());
        transportEntity.setParamBytes(command.getBytes());
        socketMutualService.request(transportEntity, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(TransportEntity transportEntity) {
        String respStr = new String(transportEntity.getParamBytes());
        int beginIdx = respStr.indexOf("/");
        int endIdx = respStr.indexOf("_");
        String cmdMark = respStr.substring(beginIdx+1,endIdx);
        String value = respStr.substring(endIdx+1,respStr.indexOf("\\r"));
        FrameRespData respData = new FrameRespData();
        ParaInfo paraDetil = null;
        respData.setCmdMark(cmdMark);
        respData.setAccessType(paraDetil.getNdpaAccessRight());
        respData.setDevNo(transportEntity.getDevInfo().getDevNo());
        respData.setDevType(transportEntity.getDevInfo().getDevType());
        respData.setOperType(SysConfigConstant.OPREATE_QUERY_RESP);
        List<FrameParaData> frameParaDatas = new ArrayList<>();
        FrameParaData frameParaData = new FrameParaData();
        frameParaData.setParaNo(paraDetil.getNdpaNo());
        frameParaData.setParaVal(value);
        frameParaData.setDevType(transportEntity.getDevInfo().getDevType());
        frameParaData.setDevNo(transportEntity.getDevInfo().getDevNo());
        frameParaDatas.add(frameParaData);
        respData.setFrameParaList(frameParaDatas);
        return respData;
    }

}
