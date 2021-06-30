package com.xy.netdev.WorkPlan.service.impl;

import com.xy.netdev.WorkPlan.entity.NtdvWorkPlan;
import com.xy.netdev.WorkPlan.mapper.NtdvWorkPlanMapper;
import com.xy.netdev.WorkPlan.service.INtdvWorkPlanService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class NtdvWorkPlanServiceImpl extends ServiceImpl<NtdvWorkPlanMapper, NtdvWorkPlan> implements INtdvWorkPlanService {


}
