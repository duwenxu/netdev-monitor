package com.xy.netdev.container;

import com.xy.netdev.monitor.service.IBaseInfoService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * <p>
 *基础信息容器装载类
 * </p>
 *
 * @author sunchao
 * @since 2021-03-09
 */
public class BaseContainerLoader {

    @Autowired
    private IBaseInfoService baseInfoService;

    /**
     * 初始化信息加载
     */
    @PostConstruct
    public void load(){
        //初始化所有设备信息
        DevInfoContainer.init(baseInfoService.list());
        //初始化各设备日志
        DevLogInfoContainer.init();
    }
}
