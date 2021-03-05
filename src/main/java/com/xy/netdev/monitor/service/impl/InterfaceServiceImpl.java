package com.xy.netdev.monitor.service.impl;

import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.mapper.InterfaceMapper;
import com.xy.netdev.monitor.service.IInterfaceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

/**
 * 设备接口 服务实现类
 *
 * @author admin
 * @date 2021-03-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class InterfaceServiceImpl extends ServiceImpl<InterfaceMapper, Interface> implements IInterfaceService {

}
