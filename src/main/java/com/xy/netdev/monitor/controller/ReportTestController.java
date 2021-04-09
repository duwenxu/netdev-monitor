package com.xy.netdev.monitor.controller;

import com.xy.common.model.Result;
import com.xy.common.util.DateUtils;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.DevAlertInfoContainer;
import com.xy.netdev.monitor.entity.AlertInfo;
import com.xy.netdev.rpt.bo.RptHeadDev;
import com.xy.netdev.rpt.enums.StationCtlRequestEnums;
import com.xy.netdev.rpt.service.IDevStatusReportService;
import com.xy.netdev.rpt.service.IUpRptPrtclAnalysisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author luo
 * @date 2021/4/9
 */


@Api(value = "站控上报测试", tags = "站控上报测试")
@RestController
@RequestMapping("/rpt/test")
public class ReportTestController {

    @Autowired
    IDevStatusReportService devStatusReportService;
    @Autowired
    ISysParamService sysParamService;
    @Autowired
    IUpRptPrtclAnalysisService upRptPrtclAnalysisService;


    @ApiOperation(value = "设备状态上报", notes = "设备状态上报")
    @GetMapping(value = "/status")
    public Result testStatusRpt(String devNo,String type,String status){
        switch (type){
            case "0": //上报中断状态
                devStatusReportService.rptInterrupted(devNo,status);
                break;
            case "1": //上报主备状态
                devStatusReportService.rptMasterOrSlave(devNo,status);
                break;
            case "2": //上报设备启用主备
                devStatusReportService.rptUseStandby(devNo,status);
                break;
            case "3": //上报设备告警
                devStatusReportService.rptWarning(devNo,status);
                break;
            case "5": //上报设备工作状态
                devStatusReportService.rptWorkStatus(devNo,status);
                break;
            default:break;
        }
        Result result = new Result();
        result = result.success("状态上报成功！");
        return result;
    }

    @ApiOperation(value = "设备告警上报", notes = "设备告警上报")
    @GetMapping(value = "/warning")
    public Result testWarningRpt(){
        AlertInfo alertInfo = new AlertInfo().builder()
                .devType("0020006")
                .alertLevel("1")
                .devNo("8")
                .alertTime(DateUtils.now())
                .alertNum(1)
                .ndpaNo("11")
                .alertStationNo("10")
                .alertDesc("设备：切换单元参数：变频器总状态报警：error!").build();
        DevAlertInfoContainer.addAlertInfo(alertInfo);
        RptHeadDev rptHeadDev = crateRptHeadDev(alertInfo);
        upRptPrtclAnalysisService.queryParaResponse(rptHeadDev);
        Result result = new Result();
        result = result.success("告警上报成功！");
        return result;
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
