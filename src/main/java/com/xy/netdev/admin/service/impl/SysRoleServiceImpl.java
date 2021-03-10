package com.xy.netdev.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.netdev.admin.entity.SysMenu;
import com.xy.netdev.admin.entity.SysRole;
import com.xy.netdev.admin.entity.SysRoleMenu;
import com.xy.netdev.admin.mapper.SysRoleMapper;
import com.xy.netdev.admin.service.ISysRoleMenuService;
import com.xy.netdev.admin.service.ISysRoleService;
import com.xy.netdev.admin.vo.SysRoleMenuModel;
import com.xy.netdev.common.constant.SysConfigConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 角色信息 服务实现类
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Resource
    SysRoleMapper sysRoleMapper;
    @Autowired
    ISysRoleMenuService sysRoleMenuService;

    @Override
    public List<SysRole> getUserRoles(Integer userId) {
        Map<String,Object> queryMap = new HashMap<String,Object>();
        queryMap.put("userId",userId);
        queryMap.put("roleStatus", SysConfigConstant.STATUS_OK);
        return sysRoleMapper.getUserRoles(queryMap);
    }


    @Transactional
    @Override
    public boolean updateRoleMenus(SysRoleMenuModel roleMenu) {
        boolean flag = false;
        Integer roleId = roleMenu.getRoleId();
        List<SysMenu> roleMenus = roleMenu.getRoleMenus();
        if(null != roleId && roleMenus.size() >0){
            QueryWrapper<SysRoleMenu> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ROLE_ID",roleId);
            sysRoleMenuService.remove(queryWrapper);
            List<SysRoleMenu> sysRoleMenus = new ArrayList<>();
            roleMenus.forEach(sysMenu -> {
                SysRoleMenu sysRoleMenu = new SysRoleMenu();
                sysRoleMenu.setRoleId(roleId);
                sysRoleMenu.setMenuId(sysMenu.getMenuId());
                sysRoleMenus.add(sysRoleMenu);
            });
            flag =  sysRoleMenuService.saveBatch(sysRoleMenus);
        }
        return flag;
    }

    /**
     * @功能：根据删除菜单更新角色权限信息
     * @param menuId
     * @return
     */
    @Override
    public boolean updRolesByMenuDeleted(Integer menuId) {
        QueryWrapper<SysRoleMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("MENU_ID",menuId);
        return sysRoleMenuService.remove(queryWrapper);
    }
}
