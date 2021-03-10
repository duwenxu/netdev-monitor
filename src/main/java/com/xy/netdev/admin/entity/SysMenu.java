package com.xy.netdev.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.xy.common.annotation.KeyCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 菜单信息表
 * </p>
 *
 * @author liufq
 * @since 2019-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("XY_SYS_MENU")
@ApiModel(value = "SysMenu对象", description = "菜单信息表")
public class SysMenu extends Model<SysMenu> {

    private static final long serialVersionUID = 1L;

    @KeyCode
    @ApiModelProperty(value = "菜单ID")
    @TableId(value = "MENU_ID", type = IdType.AUTO)
    private Integer menuId;


    @ApiModelProperty(value = "菜单名称")
    @TableField("MENU_NAME")
    private String menuName;

    @ApiModelProperty(value = "菜单父ID")
    @TableField("MENU_PARENT_ID")
    private Integer menuParentId;

    @ApiModelProperty(value = "菜单路径")
    @TableField("MENU_PATH")
    private String menuPath;

    @ApiModelProperty(value = "菜单类型(0:一级菜单; 1:子菜单:2:按钮权限)")
    @TableField("MENU_TYPE")
    private Integer menuType;

    @ApiModelProperty(value = "菜单状态")
    @TableField("MENU_STATUS")
    private String menuStatus;

    @ApiModelProperty(value = "菜单权限编码")
    @TableField("MENU_PERMS")
    private String menuPerms;

    @ApiModelProperty(value = "权限策略1显示2禁用")
    @TableField("MENU_PERMS_TYPE")
    private String menuPermsType;

    @ApiModelProperty(value = "菜单图标")
    @TableField("MENU_ICON")
    private String menuIcon;

    @ApiModelProperty(value = "菜单标题")
    @TableField("MENU_TITLE")
    private String menuTitle;

    @ApiModelProperty(value = "组件")
    @TableField("MENU_COMPONENT")
    private String menuComponent;

    @ApiModelProperty(value = "是否隐藏路由: 0否,1是")
    @TableField("MENU_HIDDEN")
    private boolean menuHidden;

    @ApiModelProperty(value = "是否隐藏面包屑: 0否,1是")
    @TableField("MENU_BREAD")
    private boolean menuBread;

    @ApiModelProperty(value = "是否加入缓存: 0否,1是")
    @TableField("MENU_CACHE")
    private boolean menuCache;

    @ApiModelProperty(value = "'是否子节点: 0否,1是'")
    @TableField("MENU_LEAF")
    private boolean menuLeaf;


    @ApiModelProperty(value = "菜单序号")
    @TableField("MENU_SEQ")
    private String menuSeq;

    @ApiModelProperty(value = "创建日期")
    @TableField("MENU_DATE")
    private String menuDate;

    @ApiModelProperty(value = "创建人")
    @TableField("MENU_UESRID")
    private Integer menuUesrid;

    @ApiModelProperty(value = "备注一")
    @TableField("REMARK1")
    private String remark1;

    @ApiModelProperty(value = "备注二")
    @TableField("REMARK2")
    private String remark2;

    @ApiModelProperty(value = "备注三")
    @TableField("REMARK3")
    private String remark3;

    @ApiModelProperty(value = "是否为角色选中的菜单")
    @TableField(exist = false)
    private boolean checked;

    @ApiModelProperty(value = "所属角色")
    @TableField(exist = false)
    private List<String> menuAuth;


    @Override
    protected Serializable pkVal() {
        return this.menuId;
    }

}
