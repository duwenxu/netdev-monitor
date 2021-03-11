package com.xy.netdev.frame.service.bpq;

import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.transit.IDataReciveService;
import com.xy.netdev.transit.IDevAlarmReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * 39所Ku&L下变频器参数协议解析
 *
 * @author luo
 * @date 2021-03-05
 */
@Component
public class BpqPrtcServiceImpl implements IParaPrtclAnalysisService {


    /**用户命令起始标记*/
    public final static String SEND_START_MARK = "<";
    /**设备响应开始标记*/
    public final static String RESP_START_MARK = ">";


    @Autowired
    SocketMutualService socketMutualService;
    @Autowired
    IDataReciveService dataReciveService;
    @Autowired
    IDevAlarmReportService devAlarmReportService;


    /**
     * 查询设备参数
     * @param  reqInfo   请求参数信息
     */
    @Override
    public void queryPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(SEND_START_MARK).append(reqInfo.getDevNo()).append("/")
                .append(reqInfo.getCmdMark());
        String command = sb.toString();
        reqInfo.setParamBytes(command.getBytes());
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    /**
     * 查询设备参数响应
     * @param  respData   数据传输对象
     * @return
     */
    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        String respStr = new String(respData.getParamBytes());
        int beginIdx = respStr.indexOf("/");
        int endIdx = respStr.indexOf("_");
        String cmdMark = respStr.substring(beginIdx+1,endIdx);
        String value = respStr.substring(endIdx+1,respStr.indexOf("\\r"));
        ParaInfo paraDetil = null;
        respData.setCmdMark(cmdMark);
        respData.setAccessType(paraDetil.getNdpaAccessRight());
        respData.setDevNo(respData.getDevNo());
        respData.setDevType(respData.getDevType());
        respData.setOperType(SysConfigConstant.OPREATE_QUERY_RESP);
        List<FrameParaData> frameParaDatas = new ArrayList<>();
        FrameParaInfo frameParaDetail = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),respData.getCmdMark());
        FrameParaData frameParaData = new FrameParaData();
        frameParaData.setParaNo(frameParaDetail.getParaNo());
        frameParaData.setParaVal(value);
        frameParaData.setDevType(frameParaDetail.getDevType());
        frameParaData.setDevNo(frameParaDetail.getDevNo());
        frameParaDatas.add(frameParaData);
        respData.setFrameParaList(frameParaDatas);
        dataReciveService.paraQueryRecive(respData);
        //生成报警信息
        devAlarmReportService.generateAlarmInfo(respData);
        return respData;
    }

    /**
     * 设置设备参数
     * @param  reqInfo   请求参数信息
     */
    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(SEND_START_MARK).append(reqInfo.getDevNo()).append("/").append(reqInfo.getCmdMark())
                .append("_").append(reqInfo.getFrameParaList().get(0).getParaVal());
        String command = sb.toString();
        BaseInfo baseInfo = BaseInfoContainer.getDevInfoByNo(reqInfo.getDevNo());
        reqInfo.setParamBytes(command.getBytes());
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    /**
     * 设置设备参数响应
     * @param  respData   数据传输对象
     * @return
     */
    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        String respStr = new String(respData.getParamBytes());
        int beginIdx = respStr.indexOf("/");
        int endIdx = respStr.indexOf("_");
        String cmdMark = respStr.substring(beginIdx+1,endIdx);
        String value = respStr.substring(endIdx+1,respStr.indexOf("\\r"));
        FrameParaInfo frameParaDetail = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),respData.getCmdMark());
        respData.setCmdMark(cmdMark);
        respData.setAccessType(SysConfigConstant.ACCESS_TYPE_PARAM);
        respData.setDevNo(respData.getDevNo());
        respData.setDevType(respData.getDevType());
        respData.setOperType(SysConfigConstant.OPREATE_QUERY_RESP);
        List<FrameParaData> frameParaDatas = new ArrayList<>();
        FrameParaData frameParaData = new FrameParaData();
        frameParaData.setParaNo(frameParaDetail.getParaNo());
        frameParaData.setParaVal(value);
        frameParaData.setDevType(frameParaDetail.getDevType());
        frameParaData.setDevNo(frameParaDetail.getDevNo());
        frameParaDatas.add(frameParaData);
        respData.setFrameParaList(frameParaDatas);
        dataReciveService.paraCtrRecive(respData);
        //生成报警信息
        devAlarmReportService.generateAlarmInfo(respData);
        return respData;
    }


}
