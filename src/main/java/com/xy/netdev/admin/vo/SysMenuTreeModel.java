package com.xy.netdev.admin.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.xy.netdev.admin.entity.SysMenu;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 类描述
 * </p>
 *
 * @author luoqilong
 * @since 2019/12/25
 */
public class SysMenuTreeModel {

    @ApiModelProperty(value = "菜单ID")
    private Integer menuId;

    @ApiModelProperty(value = "菜单名称")
    private String menuName;

    @ApiModelProperty(value = "菜单父ID")
    private Integer menuParentId;

    @ApiModelProperty(value = "菜单路径")
    private String menuPath;

    @ApiModelProperty(value = "菜单类型(0:一级菜单; 1:子菜单:2:按钮权限)")
    private Integer menuType;

    @ApiModelProperty(value = "菜单图标")
    private String menuIcon;

    @ApiModelProperty(value = "菜单标题")
    private String menuTitle;

    @ApiModelProperty(value = "组件")
    private String menuComponent;

    @ApiModelProperty(value = "是否隐藏路由: 0否,1是")
    private boolean menuhidden;

    @ApiModelProperty(value = "是否隐藏面包屑: 0否,1是")
    private boolean menuBread;

    @ApiModelProperty(value = "是否加入缓存: 0否,1是")
    private boolean menuCache;

    @ApiModelProperty(value = "'是否子节点: 0否,1是'")
    private boolean menuLeaf;

    @ApiModelProperty(value = "菜单序号")
    private String menuSeq;

    @ApiModelProperty(value = "菜单子节点")
    private List<SysMenuTreeModel> children;

    private String title;

    private boolean expand;

    private boolean checked;

    @JSONField(name="_loading")
    private boolean _loading;

    public SysMenuTreeModel() {

    }

    public SysMenuTreeModel(SysMenu sysMenu) {
        this.menuId = sysMenu.getMenuId();
        this.menuName = sysMenu.getMenuName();
        this.menuPath = sysMenu.getMenuPath();
        this.menuType = sysMenu.getMenuType();
        this.menuSeq = sysMenu.getMenuSeq();
        this.menuIcon = sysMenu.getMenuIcon();
        this.menuComponent = sysMenu.getMenuComponent();
        this.menuParentId = sysMenu.getMenuParentId();
        this.menuTitle = sysMenu.getMenuTitle();
        this.menuLeaf = sysMenu.isMenuLeaf();
        this.menuhidden = sysMenu.isMenuHidden();
        this.menuBread = sysMenu.isMenuBread();
        this.menuCache = sysMenu.isMenuCache();
        this.checked = sysMenu.isChecked();
        if(!sysMenu.isMenuLeaf()) {
            this.children = new ArrayList<SysMenuTreeModel>();
            this._loading = false;
        }
        this.title = sysMenu.getMenuTitle();
        this.expand = false;
    }

    public Integer getMenuId() {
        return menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public Integer getMenuParentId() {
        return menuParentId;
    }

    public String getMenuPath() {
        return menuPath;
    }

    public Integer getMenuType() {
        return menuType;
    }

    public String getMenuIcon() {
        return menuIcon;
    }

    public String getMenuTitle() {
        return menuTitle;
    }

    public String getMenuComponent() {
        return menuComponent;
    }

    public boolean isMenuhidden() {
        return menuhidden;
    }

    public boolean isMenuLeaf() {
        return menuLeaf;
    }

    public String getMenuSeq() {
        return menuSeq;
    }

    public List<SysMenuTreeModel> getChildren() {
        return children;
    }

    public String getTitle() {
        return title;
    }

    public boolean isExpand() {
        return expand;
    }

    public boolean isChecked(){
        return checked;
    }

    public void setChecked(boolean checked){
        this.checked = checked;
    }


    public boolean is_loading() {
        return _loading;
    }

    public void set_loading(boolean _loading) {
        this._loading = _loading;
    }
}
