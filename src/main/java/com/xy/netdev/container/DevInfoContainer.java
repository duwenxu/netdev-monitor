package com.xy.netdev.container;

import com.xy.netdev.monitor.entity.BaseInfo;

import java.util.*;

/**
 * <p>
 *设备信息容器类
 * </p>
 *
 * @author tangxl
 * @since 2021-03-08
 */
public class DevInfoContainer {
    /**
     * 设备MAP K设备IP地址 V设备状态
     */
    private static Map<String, String> devMap = new HashMap<>();

    /**
     * 初始化所有设备信息
     * @param devs
     */
    public static void init(Collection<BaseInfo> devs){
        devs.forEach(baseInfo -> {
            devMap.put(baseInfo.getDevIpAddr(),baseInfo.getDevStatus());
        });
    }


    /**
     * @功能：添加设备信息
     * @param devAddress  设备ip地址
     * @return
     */
    public static void addDevMap(String devAddress,String devStatus) {
        devMap.put(devAddress,devStatus);
    }


    /**
     * @功能：根据设备IP地址 获取设备信息
     * @param devAddress  设备ip地址
     * @return 设备状态
     */
    public static String getDevInfo(String devAddress){
        return devMap.get(devAddress);
    }

    /**
     * @功能：根据设备IP地址 获取设备信息
     * @return 设备的所有ip地址
     */
    public static Set<String> getAllDevAddress(){
        return devMap.keySet();
    }
}
