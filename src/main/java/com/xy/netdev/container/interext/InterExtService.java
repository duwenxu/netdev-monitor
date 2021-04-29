package com.xy.netdev.container.interext;


import com.xy.netdev.monitor.bo.InterfaceViewInfo;
import com.xy.netdev.monitor.bo.ParaViewInfo;

import java.util.List;


/**
 * 控制接口缓存扩展
 *
 * @author tangxl
 * @date 2021-04-25
 */
public interface InterExtService {

    /**
     * 设置显示组装控制接口列表
     * @param devNo            设备编号
     */
    void setCacheDevInterViewInfo(String devNo);

    /**
     * 获取设备组装控制接口列表
     * @param devNo            设备编号
     */
    List<InterfaceViewInfo> getCacheDevInterViewInfo(String devNo);
}
