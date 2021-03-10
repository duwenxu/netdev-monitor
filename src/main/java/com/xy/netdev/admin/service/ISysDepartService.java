package com.xy.netdev.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.netdev.admin.entity.SysDepart;

import java.util.List;

/**
 * <p>
 * 部门信息 服务类
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
public interface ISysDepartService extends IService<SysDepart> {

    /**
     * 查询SysDepart集合
     * @param userId
     * @return
     */
    public List<SysDepart> queryUserDeparts(Integer userId);
}
