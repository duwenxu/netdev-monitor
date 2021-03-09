package com.xy.netdev.monitor.service;

import com.xy.netdev.monitor.entity.Interface;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.netdev.monitor.bo.TransUiData;

import java.util.List;

/**
 * 设备接口 服务类
 *
 * @author admin
 * @date 2021-03-05
 */
public interface IInterfaceService extends IService<Interface> {

    /**
     * 设备接口已绑定参数列表
     * @param id 设备接口id
     * @return
     */
    List<TransUiData> getlLinkedParams(String id);

    /**
     * 设备接口未绑定参数列表
     * @param id 设备接口id
     * @return
     */
    List<TransUiData> getUnlinkedParams(String id);
}
