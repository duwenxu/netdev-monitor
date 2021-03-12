package com.xy.netdev.container;


import com.xy.netdev.monitor.bo.DevStatusInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *设备状态信息容器类
 * </p>
 *
 * @author tangxl
 * @since 2021-03-12
 */
public class DevStatusContainer {

    /**
     * 设备日志信息MAP K设备编号 V设备状态信息
     */
    private static Map<String,DevStatusInfo> devStatusMap = new HashMap<>();

    /**
     * @功能：当系统启动时,进行初始化各设备报警信息
     */
    public static void init(){
        BaseInfoContainer.getDevNos().forEach(devNo -> {
            devStatusMap.put(devNo,new DevStatusInfo());
        });
    }

    /**
     * @功能：添加设备中断状态
     * @param isInterrupt    中断状态
     * @return
     */
    public synchronized static void setInterrupt(String devNo,String isInterrupt) {
        devStatusMap.get(devNo).setIsInterrupt(isInterrupt);
    }

    /**
     * @功能：添加设备报警状态
     * @param isAlarm    报警状态
     * @return
     */
    public synchronized static void setAlarm(String devNo,String isAlarm) {
        devStatusMap.get(devNo).setIsAlarm(isAlarm);
    }

    /**
     * @功能：添加设备启用主备状态
     * @param isUseStandby    启用主备状态
     * @return
     */
    public synchronized static void setUseStandby(String devNo,String isUseStandby) {
        devStatusMap.get(devNo).setIsUseStandby(isUseStandby);
    }


    /**
     * @功能：添加设备主用还是备用状态
     * @param masterOrSlave    主用还是备用状态
     * @return
     */
    public synchronized static void setMasterOrSlave(String devNo,String masterOrSlave) {
        devStatusMap.get(devNo).setMasterOrSlave(masterOrSlave);
    }


    /**
     * @功能：添加设备工作状态
     * @param workStatus    工作状态
     * @return
     */
    public synchronized static void setWorkStatus(String devNo,String workStatus) {
        devStatusMap.get(devNo).setWorkStatus(workStatus);
    }

    /**
     * @功能：清空当前状态
     * @return
     */
    public static void clear(){
         init();
    }

    /**
     * @功能：获取设备状态上报信息
     * @return  设备状态列表
     */
    public static List<DevStatusInfo> getDevAlertInfoList(){
        return new ArrayList(devStatusMap.values());
    }

}
