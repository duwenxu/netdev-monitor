package com.xy.netdev.container.paraext;


import com.xy.netdev.monitor.bo.ParaViewInfo;
import java.util.List;


/**
 * 参数缓存扩展
 *
 * @author tangxl
 * @date 2021-04-23
 */
public interface IParaExtService  {

    /**
     * 设置显示设备参数
     * @param devNo            设备编号
     */
    void setCacheDevParaViewInfo(String devNo);

    /**
     * 获取设备显示参数
     * @param devNo            设备编号
     */
    List<ParaViewInfo> getCacheDevParaViewInfo(String devNo);
}
