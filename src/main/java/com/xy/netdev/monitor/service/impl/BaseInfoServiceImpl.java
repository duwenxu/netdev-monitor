package com.xy.netdev.monitor.service.impl;

import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.mapper.BaseInfoMapper;
import com.xy.netdev.monitor.service.IBaseInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

/**
 * 设备信息 服务实现类
 *
 * @author admin
 * @date 2021-03-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BaseInfoServiceImpl extends ServiceImpl<BaseInfoMapper, BaseInfo> implements IBaseInfoService {

}