package com.xy.netdev.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 角色菜单信息
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("XY_SYS_ROLE_MENU")
@ApiModel(value = "SysRoleMenu对象", description = "角色菜单信息")
public class SysRoleMenu extends Model<SysRoleMenu> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "角色ID")
    @TableId("ROLE_ID")
    private Integer roleId;

    @ApiModelProperty(value = "菜单ID")
    @TableField("MENU_ID")
    private Integer menuId;


    @Override
    protected Serializable pkVal() {
        return this.roleId;
    }

}
