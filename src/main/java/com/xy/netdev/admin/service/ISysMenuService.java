package com.xy.netdev.admin.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.common.model.Result;
import com.xy.netdev.admin.entity.SysMenu;
import com.xy.netdev.admin.entity.SysRole;

import java.util.Map;

/**
 * <p>
 * 菜单信息表 服务类
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
public interface ISysMenuService extends IService<SysMenu> {


    /**
     * @功能：获取用户权限菜单
     * @return
     */
    public JSONObject getMenuByUser(Integer userId);

    /**
     * @功能：获取菜单树
     * @return
     */
    public JSONObject queryAllMenuTree(SysMenu sysMenu);

    /**
     * @功能：根据用户角色获取菜单树
     * @param sysRole
     * @return
     */
    public Map<String,Object> queryMenuTree(SysRole sysRole);


    public Result<SysMenu> add(SysMenu sysMenu);

    /**
     * @功能：删除菜单
     * @param sysMenu
     * @return
     */
    public Result<SysMenu> delete(SysMenu sysMenu);

}
