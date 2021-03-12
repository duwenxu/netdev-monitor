package com.xy.netdev.rpt.schedule;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.service.IBaseInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.xy.netdev.common.constant.SysConfigConstant.DEV_QUERY_INTERVAL;
import static com.xy.netdev.common.constant.SysConfigConstant.DEV_REPORT_INTERVAL;

/**
 * 状态上报查询辅助类
 *
 * @author duwenxu
 * @create 2021-03-11 9:01
 */
@Component
@Order(100)
public class ScheduleReportHelper {

    @Autowired
    private IBaseInfoService baseInfoService;

    @Autowired
    private ISysParamService sysParamService;

    private static final List<BaseInfo> availableBases = new ArrayList<>();
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
        List<BaseInfo> baseInfos = baseInfoService.list().stream().filter(base -> base.getDevStatus().equals(SysConfigConstant.DEV_STATUS_NEW)).collect(Collectors.toList());
        availableBases.addAll(baseInfos);
    }

    public static List<BaseInfo> getAvailableBases() {
        return availableBases;
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
