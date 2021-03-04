package com.xy.netdev.admin.mapper;

import com.xy.netdev.admin.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Resource;

/**
 * <p>
 * 用户信息 Mapper 接口
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 通过用户账号查询用户信息
     * @param userName
     * @return
     */
    public SysUser getUserByName(@Param("userName") String userName);

    /**
     * 根据用户名设置部门ID
     * @param username
     * @param orgId
     */
    public void updateUserDepart(@Param("userName") String username,@Param("departId") Integer orgId);

    /**
     * 根据手机号查询用户信息
     * @param phone
     * @return
     */
    public SysUser getUserByPhone(@Param("phone") String phone);

}
