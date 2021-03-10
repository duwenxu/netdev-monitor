package com.xy.netdev.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xy.netdev.admin.entity.SysRole;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 角色信息 Mapper 接口
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {


    /**
     * @功能：通过用户账号查询用户信息
     * @param queryMap
     * @return
     */
    public List<SysRole> getUserRoles(Map<String,Object> queryMap);


    public List<String> getRolesByMenu(Map<String,Object> queryMap);


}
