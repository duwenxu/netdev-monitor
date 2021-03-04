package com.xy.netdev.admin.service;

import com.xy.netdev.admin.entity.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.netdev.admin.vo.SysRoleMenuModel;

import java.util.List;

/**
 * <p>
 * 角色信息 服务类
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
public interface ISysRoleService extends IService<SysRole> {


    /**
     * @功能：获取用户的角色信息
     * @param userId
     * @return
     */
    public List<SysRole> getUserRoles(Integer userId);


    /**
     * @功能：更新角色关联菜单
     * @param roleMenu
     * @return
     */
    public boolean updateRoleMenus(SysRoleMenuModel roleMenu);


    /**
     * @功能：根据删除菜单更新角色权限信息
     * @param menuId
     * @return
     */
    public boolean updRolesByMenuDeleted(Integer menuId);
}
