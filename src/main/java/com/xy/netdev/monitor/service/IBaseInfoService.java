package com.xy.netdev.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.netdev.monitor.entity.BaseInfo;

import java.util.Map;

/**
 * 设备信息 服务类
 *
 * @author admin
 * @date 2021-03-05
 */
public interface IBaseInfoService extends IService<BaseInfo> {

    /**
     * 设备导航Map
     * @return 设备信息map结构
   */
    Map<String,Object> baseInfoMenuMap();

    /**
     * 下载设备模型文件
     * @return
     */
    Map<String, Object> downDevFile();
}
