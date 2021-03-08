package com.xy.netdev.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xy.netdev.admin.entity.SysUserRole;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 用户角色信息 Mapper 接口
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
@Component
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    @Select("select role_code from sys_role where id in (select role_id from sys_user_role where user_id = (select id from sys_user where username=#{username}))")
    List<String> getRoleByUserName(@Param("username") String username);
}
