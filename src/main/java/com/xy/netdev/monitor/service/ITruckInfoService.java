package com.xy.netdev.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.netdev.monitor.bo.TransUiData;
import com.xy.netdev.monitor.entity.TruckInfo;


import java.util.List;

public interface ITruckInfoService extends IService<TruckInfo> {


    /**
     * 通讯车已绑定设备列表
     * @param id 通讯车id
     * @return
     */
    List<TransUiData> getlLinkedParams(String id);

    /**
     * 通讯车未绑定设备列表
     * @param id 通讯车id
     * @return
     */
    List<TransUiData> getUnlinkedParams(String id);


}
