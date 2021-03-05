package com.xy.netdev.monitor.service.impl;

import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.monitor.mapper.ParaInfoMapper;
import com.xy.netdev.monitor.service.IParaInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

/**
 * 设备参数 服务实现类
 *
 * @author admin
 * @date 2021-03-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ParaInfoServiceImpl extends ServiceImpl<ParaInfoMapper, ParaInfo> implements IParaInfoService {

}
