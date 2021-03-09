package com.xy.netdev.container;

import com.xy.netdev.common.util.ParaHandlerUtil;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.ParaInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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



}
