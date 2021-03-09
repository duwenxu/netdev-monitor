package com.xy.netdev.container;

import com.xy.netdev.common.collection.FixedSizeMap;
import com.xy.netdev.common.util.DateTools;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.OperLog;
import java.util.*;

/**
 * <p>
 *设备日志信息容器类
 * </p>
 *
 * @author tangxl
 * @since 2021-03-08
 */
public class DevLogInfoContainer {

    /**
     * 设备日志信息MAP K设备编号 V按照时间排序的定长日志
     */
    private static Map<String, FixedSizeMap<String,OperLog>> devLogInfoMap = new HashMap<>();

    /**
     * @功能：当系统启动时,进行初始化各设备日志
     */
    public static void init(int devLogSize){
        BaseInfoContainer.getDevInfos().forEach(baseInfo -> {
            devLogInfoMap.put(baseInfo.getDevNo(),new FixedSizeMap<>(devLogSize));
        });
    }
    /**
     * @功能：添加设备日志信息
     * @param devLog    设备日志信息
     * @return
     */
    public synchronized static void addDevLog(OperLog devLog) {
        devLogInfoMap.get(devLog.getDevNo()).put(DateTools.getDateTime(),devLog);
    }


    /**
     * @功能：根据设备编号 获取按照时间倒序排列的设备日志列表
     * @param devNo        设备编号
     * @return  设备日志列表
     */
    public static List<OperLog> getDevLogList(String devNo){
        return new ArrayList(devLogInfoMap.get(devNo).getMap().descendingMap().values());
    }
}
