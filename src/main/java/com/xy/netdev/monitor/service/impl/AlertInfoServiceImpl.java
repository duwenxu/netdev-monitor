package com.xy.netdev.monitor.service.impl;

import com.xy.netdev.monitor.entity.AlertInfo;
import com.xy.netdev.monitor.mapper.AlertInfoMapper;
import com.xy.netdev.monitor.service.IAlertInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

/**
 * 告警信息 服务实现类
 *
 * @author admin
 * @date 2021-03-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AlertInfoServiceImpl extends ServiceImpl<AlertInfoMapper, AlertInfo> implements IAlertInfoService {

}