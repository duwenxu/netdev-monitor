package com.xy.netdev.monitor.service.impl;

import com.xy.netdev.monitor.entity.OperLog;
import com.xy.netdev.monitor.mapper.OperLogMapper;
import com.xy.netdev.monitor.service.IOperLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

/**
 * 操作日志信息 服务实现类
 *
 * @author admin
 * @date 2021-03-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OperLogServiceImpl extends ServiceImpl<OperLogMapper, OperLog> implements IOperLogService {

}
