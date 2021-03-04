package com.xy.netdev.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.netdev.admin.entity.SysDepart;
import com.xy.netdev.admin.mapper.SysDepartMapper;
import com.xy.netdev.admin.service.ISysDepartService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 部门信息 服务实现类
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
@Service
public class SysDepartServiceImpl extends ServiceImpl<SysDepartMapper, SysDepart> implements ISysDepartService {

    @Override
    public List<SysDepart> queryUserDeparts(Integer departId) {
        return baseMapper.queryUserDeparts(departId);
    }
}
