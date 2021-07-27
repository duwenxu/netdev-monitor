package com.xy.netdev.SpacePreset.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.netdev.SpacePreset.entity.NtdvSpacePreset;
import com.xy.netdev.SpacePreset.mapper.NtdvSpacePresetMapper;
import com.xy.netdev.SpacePreset.service.INtdvSpacePresetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 值班安排 服务实现类
 * </p>
 *
 * @author zb
 * @since 2021-06-09
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class NtdvSpacePresetServiceImpl extends ServiceImpl<NtdvSpacePresetMapper, NtdvSpacePreset> implements INtdvSpacePresetService {


}
