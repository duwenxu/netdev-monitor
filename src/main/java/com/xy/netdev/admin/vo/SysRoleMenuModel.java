package com.xy.netdev.admin.vo;

import com.xy.netdev.admin.entity.SysMenu;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * <p>
 * 类描述
 * </p>
 *
 * @author luoqilong
 * @since 2019/12/26
 */
public class SysRoleMenuModel {

    @ApiModelProperty(value = "角色ID")
    private Integer roleId;

    @ApiModelProperty(value = "权限菜单")
    private List<SysMenu> roleMenus;

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public List<SysMenu> getRoleMenus() {
        return roleMenus;
    }

    public void setRoleMenus(List<SysMenu> roleMenus) {
        this.roleMenus = roleMenus;
    }
}
