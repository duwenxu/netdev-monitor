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

    private static Map<String, BaseInfo> devList = new HashMap<>();

    /**
     * 初始化所有设备信息
     * @param devs
     */
    public static void init(List<BaseInfo> devs){
        devs.forEach(baseInfo -> {
            devList.put(baseInfo.getDevNo(),baseInfo);
            devMap.put(baseInfo.getDevIpAddr(),baseInfo.getDevStatus());
        });
    }


    /**
     * @功能：添加设备MAP
     * @param devNo    设备编号
     * @return
     */
    public static void addDevMap(String devNo) {

    }


    /**
     * @功能：根据设备IP地址 获取设备信息
     * @param devNo        设备编号
     * @return  设备对象
     */
    public static String   getDevInfo(String devNo){
        return devMap.get(devNo);
    }

    /**
     * @功能：获取可用的所有设备信息集合
     * @return  设备对象
     */
    public static Collection<BaseInfo> getDevInfos(){
        return devList.values();
    }

    /**
     * @功能：获取可用的所有设备编号集合
     * @return  设备对象
     */
    public static Set<String> getDevNos(){
        return devList.keySet();
    }
}
