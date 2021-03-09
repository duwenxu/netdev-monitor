package com.xy.netdev.container;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.collection.FixedSizeMap;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.DateTools;
import com.xy.netdev.monitor.entity.AlertInfo;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private static Map<String, FixedSizeMap<String, AlertInfo>> devAlertInfoMap = new HashMap<>();

    @Autowired
    public void setSysParamService(ISysParamService sysParamService){
        this.sysParamService = sysParamService;
    }

    /**
     * @功能：当系统启动时,进行初始化各设备报警信息
     */
    public static void init(){
        int devAlertInfoSize = Integer.parseInt(sysParamService.getParaRemark1(SysConfigConstant.DEV_ALERT_INFO_SZIE));
        BaseInfoContainer.getDevNos().forEach(devNo -> {
            devAlertInfoMap.put(devNo,new FixedSizeMap<>(devAlertInfoSize));
        });
    }

    /**
     * @功能：添加设备告警信息
     * @param alertInfo    设备告警信息
     * @return
     */
    public synchronized static void addAlertInfo(AlertInfo alertInfo) {
        devAlertInfoMap.get(alertInfo.getDevNo()).put(DateTools.getDateTime(),alertInfo);
    }


    /**
     * @功能：根据设备编号 和 基准时间 返回大于等于基准时间的 报警信息
     * @param devNo           设备编号
     * @param baseTime        基准时间  格式为  yyyy-MM-dd HH:mm:ss
     * @return  设备报警信息列表
     */
    public static List<AlertInfo> getDevAlertInfoList(String devNo,String baseTime){
        return new ArrayList(devAlertInfoMap.get(devNo).getMap().tailMap(baseTime).values());
    }



}
