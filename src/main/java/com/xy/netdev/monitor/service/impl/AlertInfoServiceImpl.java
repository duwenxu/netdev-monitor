package com.xy.netdev.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.netdev.monitor.entity.AlertInfo;
import com.xy.netdev.monitor.mapper.AlertInfoMapper;
import com.xy.netdev.monitor.service.IAlertInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 告警信息 服务实现类
 *
 * @author admin
 * @date 2021-03-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AlertInfoServiceImpl extends ServiceImpl<AlertInfoMapper, AlertInfo> implements IAlertInfoService {

    /**
     * 查询指定设备时间范围内的告警信息
     * @param devNo
     * @param startTime
     * @param endTime
     */
    @Override
    public IPage<AlertInfo> queryAlterInfoByDevNoTime(String devNo, String startTime, String endTime,Page page) {
        QueryWrapper<AlertInfo> queryWrapper = new QueryWrapper();
        if(StringUtils.isNotEmpty(devNo)){
            queryWrapper.eq("DEV_NO",devNo);
        }
        if(StringUtils.isNotEmpty(startTime)){
            queryWrapper.ge("ALERT_TIME",startTime);
        }
        if(StringUtils.isNotEmpty(endTime)){
            queryWrapper.and(alertInfoQueryWrapper -> alertInfoQueryWrapper.le("ALERT_TIME",endTime));
        }
        queryWrapper.orderByDesc("ALERT_TIME");
        return this.baseMapper.selectPage(page,queryWrapper);
    }
}
