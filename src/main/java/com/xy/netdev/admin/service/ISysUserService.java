package com.xy.netdev.admin.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.common.model.Result;
import com.xy.netdev.admin.entity.SysUser;

/**
 * <p>
 * 用户信息 服务类
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
public interface ISysUserService extends IService<SysUser> {


    /**
     * @功能：查询用户分页列表
     * @param page
     * @param queryWrapper
     * @return
     */
    public IPage<SysUser> queryUserPageList(IPage<SysUser> page, Wrapper<SysUser> queryWrapper);

    /**
     * @功能：校验用户是否有效
     * @param user
     * @return
     */
    public Result addUser(SysUser user);

    public Result editUser(SysUser user);

    /**
     * @功能：获取用户名称
     * @param username
     * @return
     */
    public SysUser getUserByName(String username);

    /**
     * 校验用户是否有效
     * @param sysUser
     * @return
     */
    public Result checkUserIsEffective(SysUser sysUser);

    /**
     * 根据用户名设置部门ID
     * @param username
     * @param orgId
     */
    void updateUserDepart(String username,Integer orgId);

    /**
     * 根据手机号获取用户名和密码
     */
    public SysUser getUserByPhone(String phone);

}
