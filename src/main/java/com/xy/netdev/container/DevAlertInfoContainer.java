package com.xy.netdev.container;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.collection.FixedSizeMap;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.DateTools;
import com.xy.netdev.monitor.entity.AlertInfo;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *设备告警信息容器类
 * </p>
 *
 * @author tangxl
 * @since 2021-03-08
 */
public class DevAlertInfoContainer {
    /**
     * 系统参数服务类
     */
    private static ISysParamService sysParamService;
    /**
     * 设备日志信息MAP K设备编号 V按照时间排序的定长告警信息
     */
    private static Map<String, FixedSizeMap<String, AlertInfo>> devLogInfoMap = new HashMap<>();

    @Autowired
    public void setSysParamService(ISysParamService sysParamService){
        this.sysParamService = sysParamService;
    }

    /**
     * @功能：当系统启动时,进行初始化各设备日志
     */
    public static void init(){

    }

    /**
     * @功能：添加设备告警信息
     * @param alertInfo    设备告警信息
     * @return
     */
    public synchronized static void addAlertInfo(AlertInfo alertInfo) {
        devLogInfoMap.get(alertInfo.getDevNo()).put(DateTools.getDateTime(),alertInfo);
    }



}
