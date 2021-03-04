package com.xy.netdev.admin.mapper;

import com.xy.netdev.admin.entity.SysDepart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 部门信息 Mapper 接口
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
public interface SysDepartMapper extends BaseMapper<SysDepart> {

    /**
     * 根据用户ID查询部门集合
     */
    public List<SysDepart> queryUserDeparts(@Param("departId") Integer departId);
}
