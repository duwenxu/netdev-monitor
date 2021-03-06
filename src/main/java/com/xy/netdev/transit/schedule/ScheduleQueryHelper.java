package com.xy.netdev.transit.schedule;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.service.IBaseInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.xy.netdev.common.constant.SysConfigConstant.*;

/**
 * 状态上报查询辅助类
 *
 * @author duwenxu
 * @create 2021-03-11 9:01
 */
@Component
@Order(100)
public class ScheduleQueryHelper  {

    @Autowired
    private IBaseInfoService baseInfoService;

    @Autowired
    private ISysParamService sysParamService;

    private static final List<BaseInfo> availableBases = new ArrayList<>();
    private static final List<BaseInfo> availableSnmpBases = new ArrayList<>();
    private static Long queryInterval = 1000L;
    private static Long reportInterval = 1000L;

    @PostConstruct
    public void init(){
        addAvailableBases();
        initCommonInterval();
    }

    /**
     * 初始化可用设备列表
     * @return
     */
    private void addAvailableBases() {
        List<BaseInfo> allBases = baseInfoService.list();
        List<String> deployTypes = Arrays.asList(DEV_DEPLOY_GROUP,DEV_NETWORK_GROUP);
        //所有父设备
        List<String> parentBases = allBases.stream().map(BaseInfo::getDevParentNo).distinct().collect(Collectors.toList());
        List<BaseInfo> baseInfos = allBases.stream()
                .filter(base -> base.getDevStatus().equals(SysConfigConstant.DEV_STATUS_NEW))
                .filter(base-> !parentBases.contains(base.getDevNo()))
                //增加设备查询 部署类型 条件
                .filter(base-> !deployTypes.contains(base.getDevDeployType()))
                .collect(Collectors.toList());
        availableBases.addAll(baseInfos);
        //SNMP设备
        List<BaseInfo> snmpBases = baseInfos.stream().filter(base -> SNMP.equals(base.getDevNetPtcl())).collect(Collectors.toList());
        availableSnmpBases.addAll(snmpBases);
    }

    public static List<BaseInfo> getAvailableBases() {
        return availableBases;
    }

    public static List<BaseInfo> getAvailableSnmpBases() {
        return availableSnmpBases;
    }

    /**
     * 初始化加载参数
     */
    private void initCommonInterval(){
        queryInterval = Long.parseLong(sysParamService.getParaRemark1(DEV_QUERY_INTERVAL));
        reportInterval = Long.parseLong(sysParamService.getParaRemark1(DEV_REPORT_INTERVAL));
    }

    public static Long getQueryInterval() {
        return queryInterval;
    }

    public static Long getReportInterval() {
        return reportInterval;
    }
}
