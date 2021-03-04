package com.xy.netdev.admin.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.common.model.Result;
import com.xy.common.util.ConvertUtils;
import com.xy.common.util.DateUtils;
import com.xy.netdev.admin.api.ISysBaseAPI;
import com.xy.netdev.admin.entity.SysMenu;
import com.xy.netdev.admin.entity.SysRole;
import com.xy.netdev.admin.mapper.SysMenuMapper;
import com.xy.netdev.admin.mapper.SysRoleMapper;
import com.xy.netdev.admin.service.ISysMenuService;
import com.xy.netdev.admin.service.ISysRoleService;
import com.xy.netdev.admin.vo.SysMenuTreeModel;
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
 * 菜单信息表 服务实现类
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Autowired
    ISysBaseAPI sysBaseAPI;
    @Autowired
    ISysRoleService sysRoleService;
    @Resource
    SysMenuMapper sysMenuMapper;
    @Resource
    SysRoleMapper sysRoleMapper;


    /**
     * @return
     * @功能：获取用户权限菜单
     */
    @Override
    public JSONObject getMenuByUser() {
        Integer userId = sysBaseAPI.getLoginUser().getUserId();
        List<SysMenu> metaList = queryMenuByUser(userId);
        JSONObject json = new JSONObject();
        JSONArray menuJsonArray = new JSONArray();
        this.getMenuJsonArray(menuJsonArray, metaList, null);
        json.put("menu", menuJsonArray);
        return json;
    }

    /**
     * @param userId
     * @return
     * @功能：根据用户ID查询菜单信息
     */
    private List<SysMenu> queryMenuByUser(Integer userId) {
        List<SysRole> roles = sysRoleService.getUserRoles(userId);
        List<SysMenu> sysMenus = queryMenuByRole(roles);

        return sysMenus;
    }

    private List<SysMenu> queryAllMenus() {
        LambdaQueryWrapper<SysMenu> query = new LambdaQueryWrapper<SysMenu>();
        query.eq(SysMenu::getMenuStatus, SysConfigConstant.STATUS_OK);
        query.orderByAsc(SysMenu::getMenuSeq);
        return list(query);
    }

    /**
     * @param roles
     * @return
     * @功能：根据角色查询菜单信息
     */
    public List<SysMenu> queryMenuByRole(List<SysRole> roles) {
        Map<String, Object> queryMap = new HashMap<>();
        List<Integer> roleIds = new ArrayList<>();
        roles.forEach(role ->{
            roleIds.add(role.getRoleId());
        });
        queryMap.put("menuStatus", SysConfigConstant.STATUS_OK);
        queryMap.put("roleIds", roleIds);
        List<SysMenu> menus = sysMenuMapper.queryMenuByRole(queryMap);
        for (SysMenu menu : menus) {
            List<String> menuRoles = queryRoleByMenu(menu.getMenuId());
            menu.setMenuAuth(menuRoles);
        }
        return menus;
    }

    /**
     * @param menuId
     * @return
     * @功能:根据菜单id查询拥有该菜单得角色
     */
    public List<String> queryRoleByMenu(Integer menuId) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("menuId", menuId);
        return sysRoleMapper.getRolesByMenu(queryMap);
    }

    /**
     * @param jsonArray
     * @param metaList
     * @param parentJson
     * @功能：获取菜单JSON数组
     */
    private void getMenuJsonArray(JSONArray jsonArray, List<SysMenu> metaList, JSONObject parentJson) {
        for (SysMenu menu : metaList) {
            if (menu.getMenuType() == null) {
                continue;
            }
            Integer tempPid = menu.getMenuParentId();
            JSONObject json = getMenuJsonObject(menu);
            if (json == null) {
                continue;
            }
            if (parentJson == null && tempPid == SysConfigConstant.TOP_MENU_ID) {
                jsonArray.add(json);
                if (!menu.isMenuLeaf()) {
                    getMenuJsonArray(jsonArray, metaList, json);
                }
            } else if (parentJson != null && tempPid != SysConfigConstant.TOP_MENU_ID && tempPid.intValue() == parentJson.getInteger("menuId").intValue()) {
                // 类型( 0：一级菜单 1：子菜单 2：按钮 )
                if (menu.getMenuType().equals(SysConfigConstant.MENU_TYPE_2)) {
                    JSONObject metaJson = parentJson.getJSONObject("meta");
                    if (metaJson.containsKey("menuList")) {
                        metaJson.getJSONArray("menuList").add(json);
                    } else {
                        JSONArray menuList = new JSONArray();
                        menuList.add(json);
                        metaJson.put("menuList", menuList);
                    }
                    // 类型( 0：一级菜单 1：子菜单 2：按钮 )
                } else if (menu.getMenuType().equals(SysConfigConstant.MENU_TYPE_1) || menu.getMenuType().equals(SysConfigConstant.MENU_TYPE_0)) {
                    if (parentJson.containsKey("children")) {
                        parentJson.getJSONArray("children").add(json);
                    } else {
                        JSONArray children = new JSONArray();
                        children.add(json);
                        parentJson.put("children", children);
                    }

                    if (!menu.isMenuLeaf()) {
                        getMenuJsonArray(jsonArray, metaList, json);
                    }
                }
            }

        }
    }

    /**
     * @param sysMenu
     * @return
     * @功能：获取菜单元数据信息
     */
    private JSONObject getMenuJsonObject(SysMenu sysMenu) {
        JSONObject json = new JSONObject();
        // 类型(0：一级菜单 1：子菜单 2：按钮)
        if (sysMenu.getMenuType().equals(SysConfigConstant.MENU_TYPE_2)) {
            return null;
        } else if (sysMenu.getMenuType().equals(SysConfigConstant.MENU_TYPE_0) || sysMenu.getMenuType().equals(SysConfigConstant.MENU_TYPE_1)) {
            json.put("menuId", sysMenu.getMenuId());
            // 重要规则：路由name (通过URL生成路由name,路由name供前端开发，页面跳转使用)
            if (ConvertUtils.isNotEmpty(sysMenu.getMenuName())) {
                json.put("name", sysMenu.getMenuName());
            } else {
                json.put("name", urlToRouteName(sysMenu.getMenuPath()));
            }
            json.put("component", sysMenu.getMenuComponent());
            json.put("path", sysMenu.getMenuPath());
            JSONObject meta = new JSONObject();
            meta.put("title", sysMenu.getMenuTitle());
            if (sysMenu.getMenuParentId() != SysConfigConstant.TOP_MENU_ID) {
                if (ConvertUtils.isNotEmpty(sysMenu.getMenuIcon())) {
                    meta.put("icon", sysMenu.getMenuIcon());
                }
            } else {
                if (ConvertUtils.isNotEmpty(sysMenu.getMenuIcon())) {
                    meta.put("icon", sysMenu.getMenuIcon());
                }
            }
            // 是否隐藏路由，默认都是显示的
            if (sysMenu.isMenuHidden()) {
                meta.put("hideInMenu", true);
            }
            //是否隐藏面包屑
            if(sysMenu.isMenuBread()){
                meta.put("hideInBread", true);
            }else{
                meta.put("needInBread",true);
            }
            //是否不需要加入缓存
            if(!sysMenu.isMenuCache()){
                meta.put("notCache",true);
            }else{
                meta.put("notCache",false);
            }
            meta.put("access", sysMenu.getMenuAuth());
            json.put("meta", meta);
        }
        return json;
    }

    /**
     * 通过URL生成路由name（去掉URL前缀斜杠，替换内容中的斜杠‘/’为-） 举例： URL = /isystem/role RouteName =
     * isystem-role
     *
     * @return
     */
    private String urlToRouteName(String url) {
        if (ConvertUtils.isNotEmpty(url)) {
            if (url.startsWith("/")) {
                url = url.substring(1);
            }
            url = url.replace("/", "-");

            // 特殊标记
            url = url.replace(":", "@");
            return url;
        } else {
            return null;
        }
    }

    /**
     * @return
     * @功能：获取菜单树
     */
    @Override
    public JSONObject queryAllMenuTree(SysMenu sysMenu) {
        QueryWrapper<SysMenu> query = new QueryWrapper<SysMenu>();
        query.eq("MENU_STATUS", SysConfigConstant.STATUS_OK);
        query.eq("MENU_PARENT_ID", sysMenu.getMenuParentId());
        query.orderByAsc("MENU_SEQ");
        if (ConvertUtils.isNotEmpty(sysMenu.getMenuTitle())) {
            query.like("MENU_TITLE", sysMenu.getMenuTitle());
        }
        List<SysMenu> sysMenus = list(query);
        JSONObject json = new JSONObject();
        JSONArray menuJsonArray = new JSONArray();
        for (SysMenu menu : sysMenus) {
            JSONObject node = new JSONObject();
            node.put("menuId", menu.getMenuId());
            node.put("menuName", menu.getMenuName());
            node.put("menuPath", menu.getMenuPath());
            node.put("menuType", menu.getMenuType());
            node.put("menuSeq", menu.getMenuSeq());
            node.put("menuIcon", menu.getMenuIcon());
            node.put("menuComponent", menu.getMenuComponent());
            node.put("menuParentId", menu.getMenuParentId());
            node.put("menuTitle", menu.getMenuTitle());
            node.put("menuLeaf", menu.isMenuLeaf());
            node.put("menuHidden",menu.isMenuHidden());
            node.put("menuBread",menu.isMenuBread());
            node.put("menuCache",menu.isMenuCache());
            if (!menu.isMenuLeaf()) {
                node.put("children", new ArrayList<>());
                node.put("_loading", false);
            }
            node.put("title", menu.getMenuTitle());
            menuJsonArray.add(node);
        }
        json.put("menu", menuJsonArray);
        return json;
    }


    /**
     * @param sysRole
     * @return
     * @功能：根据用户角色获取菜单树
     */
    @Override
    public Map<String, Object> queryMenuTree(SysRole sysRole) {
        Map<String, Object> result = new HashMap<>();
        List<SysRole> sysRoles = new ArrayList<>();
        sysRoles.add(sysRole);
        List<SysMenu> roleMenus = queryMenuByRole(sysRoles);
        List<SysMenu> sysMenus = queryAllMenus();
        Map<String, SysMenu> sysMenuMap = ConvertUtils.beanToMap(sysMenus);
        List<Integer> roleIds = new ArrayList<>();
        for (SysMenu roleMenu : roleMenus) {
            roleIds.add(roleMenu.getMenuId());
        }
        sysMenus.forEach(menu -> {
            if (roleIds.contains(menu.getMenuId())) {
                if (menu.isMenuLeaf() && menu.getMenuParentId() != SysConfigConstant.TOP_MENU_ID) {
                    sysMenuMap.get(String.valueOf(menu.getMenuParentId())).setChecked(false);
                }
                menu.setChecked(true);
            }
        });
        List<SysMenuTreeModel> treeList = new ArrayList<>();
        getTreeModelList(treeList, sysMenus, null);

        result.put("menuTree", treeList);
        return result;
    }

    /**
     * @param treeList
     * @param metaList
     * @param temp
     * @功能：获取菜单树节点
     */
    private void getTreeModelList(List<SysMenuTreeModel> treeList, List<SysMenu> metaList, SysMenuTreeModel temp) {
        for (SysMenu sysMenu : metaList) {
            Integer tempPid = sysMenu.getMenuParentId();
            SysMenuTreeModel tree = new SysMenuTreeModel(sysMenu);
            if (temp == null && tempPid == SysConfigConstant.TOP_MENU_ID) {
                treeList.add(tree);
                if (!tree.isMenuLeaf()) {
                    getTreeModelList(treeList, metaList, tree);
                }
            } else if (temp != null && tempPid != SysConfigConstant.TOP_MENU_ID && tempPid.equals(temp.getMenuId())) {
                temp.getChildren().add(tree);
                if (!tree.isMenuLeaf()) {
                    getTreeModelList(treeList, metaList, tree);
                }
            }
        }
    }


    /**
     * @功能：添加菜单节点，并更新父节点不为叶子节点
     * @param sysMenu
     * @return
     */
    @Override
    public Result<SysMenu> add(SysMenu sysMenu) {
        sysMenu.setMenuStatus(SysConfigConstant.STATUS_OK);
        sysMenu.setMenuDate(DateUtils.now());
        sysMenu.setMenuLeaf(true);
        boolean flag = false;
        if(sysMenu.getMenuParentId()!=0){
            sysMenu.setMenuType(SysConfigConstant.MENU_TYPE_1);
            SysMenu parentMenu = new SysMenu();
            parentMenu.setMenuId(sysMenu.getMenuParentId());
            parentMenu.setMenuLeaf(false);
            flag = updateById(parentMenu);
        }else{
            sysMenu.setMenuType(SysConfigConstant.MENU_TYPE_0);
        }
        Result<SysMenu> result = new Result<SysMenu>();
        flag= save(sysMenu);
        if(flag){
            result = result.success("保存成功！");
        }else{
            result = result.error500("保存失败！");
        }
        return result;
    }

    /**
     * @功能:删除菜单
     * @param sysMenu
     * @return
     */
    @Override
    @Transactional
    public Result<SysMenu> delete(SysMenu sysMenu) {
        boolean flag = false;
        Integer menuId = sysMenu.getMenuId();
        Result<SysMenu> result = new Result<SysMenu>();
        if(getSubNodeNum(menuId)>0){
            result.setCode(403);
            result.setMessage("当前菜单节点包含子菜单，请先删除子菜单！");
            result.setSuccess(false);
        }else{
            removeById(menuId);
            if(flag){
               flag = sysRoleService.updRolesByMenuDeleted(menuId);
            }
            result =  result.success("删除成功");
            //如果当前节点不存在兄弟节点，更新父节点为叶子节点
            if(getSibNodeNum(sysMenu.getMenuParentId())==0){
                SysMenu parentMenu = new SysMenu();
                parentMenu.setMenuId(sysMenu.getMenuParentId());
                parentMenu.setMenuLeaf(true);
                flag = updateById(parentMenu);
            }
        }
        return result;
    }

    /**
     * @功能：查询当前节点子节点个数
     * @param menuId
     * @return
     */
    private Integer getSubNodeNum(Integer menuId){
        QueryWrapper<SysMenu> query = new QueryWrapper<SysMenu>();
        query.eq("MENU_PARENT_ID", menuId);
        List<SysMenu> sysMenus = list(query);
        return sysMenus.size();
    }

    /**
     * @功能：查询当前节点兄弟节点个数
     * @param menuParentId
     * @return
     */
    private Integer getSibNodeNum(Integer menuParentId){
        QueryWrapper<SysMenu> query = new QueryWrapper<SysMenu>();
        query.eq("MENU_PARENT_ID", menuParentId);
        List<SysMenu> sysMenus = list(query);
        return sysMenus.size();
    }
}