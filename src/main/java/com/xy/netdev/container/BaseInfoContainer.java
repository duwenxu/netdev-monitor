package com.xy.netdev.container;

import com.xy.netdev.monitor.entity.BaseInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *基础信息容器类
 * </p>
 *
 * @author tangxl
 * @since 2021-03-08
 */
public class BaseInfoContainer {
    /**
     * 设备MAP K设备IP地址 V设备信息
     */
    private static Map<String, BaseInfo> devMap = new HashMap<>();


    /**
     * @功能：添加设备MAP
     * @param devList    设备列表
     * @return
     */
    public static void addDevMap(List<BaseInfo> devList) {

    }

    /**
     * @功能：根据设备IP地址 获取设备信息
     * @param devIPAddr    设备IP地址
     * @return  设备对象
     */
    public static BaseInfo getDevInfo(String devIPAddr){
        return devMap.get(devIPAddr);
    }

}
