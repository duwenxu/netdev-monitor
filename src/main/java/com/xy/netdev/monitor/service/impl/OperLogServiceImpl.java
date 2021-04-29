package com.xy.netdev.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.netdev.monitor.entity.AlertInfo;
import com.xy.netdev.monitor.entity.OperLog;
import com.xy.netdev.monitor.mapper.OperLogMapper;
import com.xy.netdev.monitor.service.IOperLogService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 操作日志信息 服务实现类
 *
 * @author admin
 * @date 2021-03-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OperLogServiceImpl extends ServiceImpl<OperLogMapper, OperLog> implements IOperLogService {

    /**
     * 查询指定设备时间范围内的日志信息
     * @return
     */
    @Override
    public IPage<OperLog> queryOperLogByDevNoTime(String devNo, String startTime, String endTime, Page page) {
        QueryWrapper<OperLog> queryWrapper = new QueryWrapper();
        if(StringUtils.isNotEmpty(devNo)){
            queryWrapper.eq("DEV_NO",devNo);
        }
        if(StringUtils.isNotEmpty(startTime)){
            queryWrapper.ge("LOG_TIME",startTime);
        }
        if(StringUtils.isNotEmpty(endTime)){
            queryWrapper.and(alertInfoQueryWrapper -> alertInfoQueryWrapper.le("LOG_TIME",endTime));
        }
        queryWrapper.orderByDesc("LOG_TIME");
        return this.baseMapper.selectPage(page,queryWrapper);
    }

    /**
     * 更新主键
     */
    @Override
    public void updateOperId() {
        this.baseMapper.updateOperId();
    }
}
