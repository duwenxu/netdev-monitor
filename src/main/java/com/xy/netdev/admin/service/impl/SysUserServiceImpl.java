package com.xy.netdev.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.common.model.Result;
import com.xy.common.util.DateUtils;
import com.xy.netdev.admin.entity.SysRole;
import com.xy.netdev.admin.entity.SysUser;
import com.xy.netdev.admin.entity.SysUserRole;
import com.xy.netdev.admin.mapper.SysUserMapper;
import com.xy.netdev.admin.service.ISysRoleService;
import com.xy.netdev.admin.service.ISysUserRoleService;
import com.xy.netdev.admin.service.ISysUserService;
import com.xy.netdev.common.constant.SysConfigConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 用户信息 服务实现类
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {


    @Autowired
    private ISysUserRoleService sysUserRoleService;
    @Autowired
    private ISysRoleService sysRoleService;
    @Resource
    private SysUserMapper userMapper;


    /**
     * @功能：查询用户分页列表
     * @param page
     * @param queryWrapper
     * @return
     */
    @Override
    public IPage<SysUser> queryUserPageList(IPage<SysUser> page, Wrapper<SysUser> queryWrapper) {
        IPage<SysUser> userPage = page(page, queryWrapper);
        for (SysUser user : userPage.getRecords()) {
            List<SysRole> roles = new ArrayList<>();
            QueryWrapper<SysUserRole> roleWrapper = new QueryWrapper<>();
            roleWrapper.eq("USER_ID",user.getUserId());
            List<SysUserRole> sysUserRoles = sysUserRoleService.list(roleWrapper);
            sysUserRoles.forEach(sysUserRole -> {
                roles.add(sysRoleService.getById(sysUserRole.getRoleId()));
            });
            user.setUserRole(roles);
        }
        return userPage;
    }

    /**
     * @功能：校验用户是否有效
     * @param user
     * @return
     */
    @Override
    @Transactional
    public Result addUser(SysUser user) {
        Result result = new Result();
        user.setUserDate(DateUtils.dateToStr(new Date(), DateUtils.FORMAT));//设置创建时间
       /* String salt = ConvertUtils.randomGen(8);
        user.setUserSalt(salt);
        String passwordEncode = PasswordUtils.encrypt(user.getUserName(), user.getUserPwd(), salt);*/
        user.setUserPwd(user.getUserPwd());
        user.setUserStatus(SysConfigConstant.STATUS_OK);
        List<SysRole> roles = user.getUserRole();
        List<SysUserRole> userRoles = new ArrayList<>();
        boolean flag= save(user);
        if(flag){
            roles.forEach(SysRole->{
                SysUserRole userRole = new SysUserRole();
                userRole.setRoleId(SysRole.getRoleId());
                userRole.setUserId(user.getUserId());
                userRoles.add(userRole);
            });
            flag = sysUserRoleService.saveBatch(userRoles);
        }
        if(flag){
            result.isSuccess();
        }
        return result;
    }

    @Override
    @Transactional
    public Result editUser(SysUser user) {
        Result result = new Result();
        List<SysRole> roles = user.getUserRole();
        List<SysUserRole> userRoles = new ArrayList<>();
        boolean flag= updateById(user);
        if(flag){
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("USER_ID",user.getUserId());
            sysUserRoleService.remove(queryWrapper);
            roles.forEach(SysRole->{
                SysUserRole userRole = new SysUserRole();
                userRole.setRoleId(SysRole.getRoleId());
                userRole.setUserId(user.getUserId());
                userRoles.add(userRole);
            });
            flag = sysUserRoleService.saveBatch(userRoles);
        }
        if(flag){
            result.isSuccess();
        }
        return result;
    }

    @Override
    public SysUser getUserByName(String username) {
        return userMapper.getUserByName(username);
    }


    @Override
    public Result checkUserIsEffective(SysUser sysUser) {
        Result<?> result = new Result<Object>();
        //情况1：根据用户信息查询，该用户不存在
        if (sysUser == null) {
            result.error500("该用户不存在，请注册");
            return result;
        }
        //情况2：根据用户信息查询，该用户已冻结
        if (SysConfigConstant.STATUS_FAIL.equals(sysUser.getUserStatus())) {
            result.error500("该用户已冻结");
            return result;
        }
        return result;
    }

    @Override
    public void updateUserDepart(String username,Integer orgId) {
        baseMapper.updateUserDepart(username, orgId);
    }

    @Override
    public SysUser getUserByPhone(String phone) {
        return userMapper.getUserByPhone(phone);
    }

}
