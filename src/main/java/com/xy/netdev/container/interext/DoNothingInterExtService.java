package com.xy.netdev.container.interext;


import com.xy.netdev.container.DevCtrlInterInfoContainer;
import com.xy.netdev.monitor.bo.InterfaceViewInfo;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 控制接口缓存扩展
 *
 * @author tangxl
 * @date 2021-04-25
 */
@Service
public class DoNothingInterExtService implements InterExtService {


    /**
     * 设置显示组装控制接口列表
     * @param devNo            设备编号
     */
    public void setCacheDevInterViewInfo(String devNo) {}

    /**
     * 获取设备组装控制接口列表
     * @param devNo            设备编号
     */
    public List<InterfaceViewInfo> getCacheDevInterViewInfo(String devNo) {
        return DevCtrlInterInfoContainer.getDevCtrInterExtList(devNo);
    }
}
