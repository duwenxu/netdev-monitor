package com.xy.netdev.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.monitor.mapper.PrtclFormatMapper;
import com.xy.netdev.monitor.service.IPrtclFormatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 协议格式 服务实现类
 *
 * @author admin
 * @date 2021-03-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PrtclFormatServiceImpl extends ServiceImpl<PrtclFormatMapper, PrtclFormat> implements IPrtclFormatService {

}
