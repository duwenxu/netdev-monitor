package com.xy.netdev.monitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.netdev.monitor.entity.AlertInfo;

import java.util.List;

/**
 * 告警信息 服务类
 *
 * @author admin
 * @date 2021-03-05
 */
public interface IAlertInfoService extends IService<AlertInfo> {

    /**
     * 查询指定设备时间范围内的告警信息
     * @param devNo
     * @param startTime
     * @param endTime
     */
    IPage<AlertInfo> queryAlterInfoByDevNoTime(String devNo, String startTime, String endTime,Page page);

    /**
     * 查询指定设备时间范围内的所有告警信息
     * @param devType
     * @param startTime
     * @param endTime
     */
    List<AlertInfo> queryPageList(String devType, String startTime, String endTime);
}
