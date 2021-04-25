package com.xy.netdev.monitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.netdev.monitor.entity.AlertInfo;
import com.xy.netdev.monitor.entity.OperLog;

/**
 * 操作日志信息 服务类
 *
 * @author admin
 * @date 2021-03-05
 */
public interface IOperLogService extends IService<OperLog> {

    /**
     * 查询指定设备时间范围内的日志信息
     * @return
     */
    IPage<OperLog> queryOperLogByDevNoTime(String devNo, String startTime, String endTime, Page page);

    /**
     * 更新主键
     */
    void updateOperId();
}
