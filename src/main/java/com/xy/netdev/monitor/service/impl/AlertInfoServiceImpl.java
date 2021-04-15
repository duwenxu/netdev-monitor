package com.xy.netdev.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.netdev.monitor.entity.AlertInfo;
import com.xy.netdev.monitor.mapper.AlertInfoMapper;
import com.xy.netdev.monitor.service.IAlertInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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
    public List<AlertInfo> queryAlterInfoByDevNoTime(String devNo, String startTime, String endTime) {
        QueryWrapper<AlertInfo> queryWrapper = new QueryWrapper();
        queryWrapper.eq("DEV_NO",devNo);
        queryWrapper.ge("ALERT_TIME",startTime);
        queryWrapper.and(alertInfoQueryWrapper -> alertInfoQueryWrapper.le("ALERT_TIME",endTime));
        return this.list(queryWrapper);
    }
}
