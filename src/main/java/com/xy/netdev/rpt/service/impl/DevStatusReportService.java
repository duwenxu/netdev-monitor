package com.xy.netdev.rpt.service.impl;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.monitor.bo.DevStatusInfo;
import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.service.IDevStatusReportService;
import com.xy.netdev.rpt.service.IUpRptPrtclAnalysisService;
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
        reportDevStatus(devStatusInfo,respData);
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
        reportDevStatus(devStatusInfo,respData);
    }


    @Override
    public void rptInterrupted(FrameReqData reqData, String status) {
        DevStatusInfo devStatusInfo = createDevStatusInfo(reqData);
        devStatusInfo.setIsInterrupt(status);
        reportDevStatus(devStatusInfo,reqData);
    }

    /**
     * 上报设备启用主备
     */
    @Override
    public void rptUseStandby(FrameRespData respData, String status) {
        DevStatusInfo devStatusInfo = createDevStatusInfo(respData);
        devStatusInfo.setIsUseStandby(status);
        reportDevStatus(devStatusInfo,respData);
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
        reportDevStatus(devStatusInfo,respData);
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
        reportDevStatus(devStatusInfo,respData);
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
     * @param respData
     */
    private void reportDevStatus(DevStatusInfo devStatusInfo,FrameRespData respData){
        RptHeadDev rptHeadDev = crateRptHeadDev(devStatusInfo);
        rptHeadDev.setCmdMarkHexStr(respData.getCmdMark());
        upRptPrtclAnalysisService.queryParaResponse(rptHeadDev);
    }

    /**
     * 上报设备状态
     * @param devStatusInfo
     * @param reqData
     */
    private void reportDevStatus(DevStatusInfo devStatusInfo,FrameReqData reqData){
        RptHeadDev rptHeadDev = crateRptHeadDev(devStatusInfo);
        rptHeadDev.setCmdMarkHexStr(reqData.getCmdMark());
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
        rptHeadDev.setDevNo(devStatusInfo.getDevNo());
        return rptHeadDev;
    }
}
