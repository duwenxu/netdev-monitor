package com.xy.netdev.rpt.service.impl;

import com.xy.common.util.DateUtils;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevAlertInfoContainer;
import com.xy.netdev.container.DevStatusContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.monitor.bo.DevStatusInfo;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.bo.TransRule;
import com.xy.netdev.monitor.entity.AlertInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.enums.StationCtlRequestEnums;
import com.xy.netdev.rpt.service.IDevStatusReportService;
import com.xy.netdev.rpt.service.IUpRptPrtclAnalysisService;
import com.xy.netdev.transit.util.DataHandlerHelper;
import com.xy.netdev.websocket.send.DevIfeMegSend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DevStatusReportService implements IDevStatusReportService {

    @Autowired
    ISysParamService sysParamService;
    @Autowired
    IUpRptPrtclAnalysisService upRptPrtclAnalysisService;


    /**
     * 上报设备告警
     * @param respData
     * @param status
     */
    @Override
    public void rptWarning(FrameRespData respData, String status) {
        DevStatusInfo devStatusInfo = createDevStatusInfo(respData);
        devStatusInfo.setIsAlarm(status);
        reportDevStatus(devStatusInfo);
    }

    /**
     * 上报设备未中断，恢复连接
     * @param respData
     * @param status
     */
    @Override
    public void rptUnInterrupted(FrameRespData respData, String status) {
        DevStatusInfo devStatusInfo = createDevStatusInfo(respData);
        devStatusInfo.setIsInterrupt(status);
        reportDevStatus(devStatusInfo);
    }

    /**
     * 上报设备中断
     * @param reqData
     * @param status
     */
    @Override
    public void rptInterrupted(FrameReqData reqData, String status) {
        DevStatusInfo devStatusInfo = createDevStatusInfo(reqData);
        devStatusInfo.setIsInterrupt(status);
        reportDevStatus(devStatusInfo);
    }

    /**
     * 上报设备启用主备
     */
    @Override
    public void rptUseStandby(FrameRespData respData, String status) {
        DevStatusInfo devStatusInfo = createDevStatusInfo(respData);
        devStatusInfo.setIsUseStandby(status);
        reportDevStatus(devStatusInfo);
    }

    /**
     * 上报设备主备状态
     * @param respData
     * @param status
     */
    @Override
    public void rptMasterOrSlave(FrameRespData respData, String status) {
        DevStatusInfo devStatusInfo = createDevStatusInfo(respData);
        devStatusInfo.setMasterOrSlave(status);
        reportDevStatus(devStatusInfo);
    }

    /**
     * 上报设备工作状态
     * @param respData
     * @param status
     */
    @Override
    public void rptWorkStatus(FrameRespData respData, String status) {
        DevStatusInfo devStatusInfo = createDevStatusInfo(respData);
        devStatusInfo.setWorkStatus(status);
        reportDevStatus(devStatusInfo);
    }

    /**
     * 生成设备基本信息
     * @param respData
     * @return
     */
    private DevStatusInfo createDevStatusInfo(FrameRespData respData){
        DevStatusInfo devStatusInfo = new DevStatusInfo();
        devStatusInfo.setDevNo(respData.getDevNo());
        devStatusInfo.setStationId(sysParamService.getParaRemark1(SysConfigConstant.PUBLIC_PARA_STATION_NO));
        devStatusInfo.setDevTypeCode(sysParamService.getParaRemark1(respData.getDevType()));
        return devStatusInfo;
    }

    /**
     * 生成设备基本信息
     * @param respData
     * @return
     */
    private DevStatusInfo createDevStatusInfo(FrameReqData respData){
        DevStatusInfo devStatusInfo = new DevStatusInfo();
        devStatusInfo.setDevNo(respData.getDevNo());
        devStatusInfo.setStationId(sysParamService.getParaRemark1(SysConfigConstant.PUBLIC_PARA_STATION_NO));
        devStatusInfo.setDevTypeCode(sysParamService.getParaRemark1(respData.getDevType()));
        return devStatusInfo;
    }

    /**
     * 上报设备状态
     * @param devStatusInfo
     */
    private void reportDevStatus(DevStatusInfo devStatusInfo){
        RptHeadDev rptHeadDev = crateRptHeadDev(devStatusInfo);
        //推送前端
        DevIfeMegSend.sendDevStatusToDev();
        //上报站控
        upRptPrtclAnalysisService.queryParaResponse(rptHeadDev);
    }



    /**
     * 创建上报数据体
     * @param devStatusInfo
     * @return
     */
    private RptHeadDev crateRptHeadDev(DevStatusInfo devStatusInfo){
        RptHeadDev rptHeadDev = new RptHeadDev();
        rptHeadDev.setStationNo(devStatusInfo.getStationId());
        List<DevStatusInfo> devStatusInfos = new ArrayList<>();
        devStatusInfos.add(devStatusInfo);
        rptHeadDev.setParam(devStatusInfos);
        rptHeadDev.setDevNum(1);
        rptHeadDev.setCmdMarkHexStr(StationCtlRequestEnums.DEV_STATUS_REPORT.getCmdCode());
        rptHeadDev.setDevNo(devStatusInfo.getDevNo());
        return rptHeadDev;
    }


    /**
     * 上报设备状态信息
     * @param respData
     * @param rules
     * @param param
     */
    public void reportWarningAndStaus(FrameRespData respData, List<TransRule> rules, FrameParaData param){
        //根据参数对应设备状态类型结合参数值上报设备状态或告警信息
        FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByNo(param.getDevType(),param.getParaNo());
        String type = paraInfo.getAlertPara();
        rules.forEach(rule->{
            if(rule.getInner().equals(param.getParaVal()) || respData.getRespCode().equals("1")) {
                String outerStatus = rule.getOuter();
                switch(type) {
                    case SysConfigConstant.DEV_STATUS_ALARM:
                        if(DevStatusContainer.setAlarm(respData.getDevNo(),outerStatus)){
                            rptWarning(respData,outerStatus);
                        }
                        //上报告警信息
                        reportDevAlertInfo(respData,paraInfo,outerStatus);
                    break;
                    case SysConfigConstant.DEV_STATUS_INTERRUPT:
                        //参数返回值是否恢复中断
                        if(DevStatusContainer.setInterrupt(respData.getDevNo(),outerStatus)){
                            rptUnInterrupted(respData,outerStatus);
                        }
                        break;
                    case SysConfigConstant.DEV_STATUS_SWITCH:
                        //参数返回值是否启用主备
                        if(DevStatusContainer.setUseStandby(respData.getDevNo(),outerStatus)){
                            rptUseStandby(respData,outerStatus);
                        }
                        break;
                    case SysConfigConstant.DEV_STATUS_STANDBY:
                        //参数返回主备状态
                        if(DevStatusContainer.setMasterOrSlave(respData.getDevNo(),outerStatus)){
                            rptMasterOrSlave(respData,outerStatus);
                        }
                        break;
                    case SysConfigConstant.DEV_STATUS_MAINTAIN:
                        //参数返回设备工作状态
                        if(DevStatusContainer.setWorkStatus(respData.getDevNo(),outerStatus)){
                            rptWorkStatus(respData,outerStatus);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 上报设备告警信息
     * @param respData
     * @param paraInfo
     * @param status
     */
    private void reportDevAlertInfo(FrameRespData respData,FrameParaInfo paraInfo, String status){
        //参数返回值是否产生告警
        if (status.equals(SysConfigConstant.RPT_DEV_STATUS_ISALARM_YES)) {
            BaseInfo baseInfo = BaseInfoContainer.getDevInfoByNo(respData.getDevNo());
            String alertDesc = DataHandlerHelper.genAlertDesc(baseInfo,paraInfo);
            AlertInfo alertInfo = new AlertInfo().builder()
                    .devType(respData.getDevType())
                    .alertLevel(paraInfo.getAlertLevel())
                    .devNo(respData.getDevNo())
                    .alertTime(DateUtils.now())
                    .alertNum(1)
                    .ndpaNo(paraInfo.getParaNo())
                    .alertStationNo(sysParamService.getParaRemark1(SysConfigConstant.PUBLIC_PARA_STATION_NO))
                    .alertDesc(alertDesc).build();
            DevAlertInfoContainer.addAlertInfo(alertInfo);
            RptHeadDev rptHeadDev = crateRptHeadDev(alertInfo);
            upRptPrtclAnalysisService.queryParaResponse(rptHeadDev);
        }
    }


    /**
     * 创建上报数据体
     * @param alertInfo
     * @return
     */
    private RptHeadDev crateRptHeadDev(AlertInfo alertInfo){
        RptHeadDev rptHeadDev = new RptHeadDev();
        rptHeadDev.setStationNo(sysParamService.getParaRemark1(SysConfigConstant.PUBLIC_PARA_STATION_NO));
        rptHeadDev.setParam(alertInfo);
        rptHeadDev.setDevNum(1);
        rptHeadDev.setCmdMarkHexStr(StationCtlRequestEnums.PARA_ALARM_REPORT.getCmdCode());
        rptHeadDev.setDevNo(alertInfo.getDevNo());
        return rptHeadDev;
    }

}
