package com.xy.netdev.admin.controller;


import com.alibaba.fastjson.JSONObject;
import com.xy.common.helper.ControllerHelper;
import com.xy.common.model.Result;
import com.xy.netdev.admin.api.ISysBaseAPI;
import com.xy.netdev.admin.entity.SysMenu;
import com.xy.netdev.admin.service.ISysMenuService;
import com.xy.netdev.common.annotation.AutoLog;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.JwtUtil;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 菜单信息表 前端控制器
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
@Api(tags = "菜单管理接口")
@RestController
@RequestMapping("/admin/sys-menu")
public class SysMenuController {

    @Autowired
    ISysMenuService service;
    @Autowired
    private ISysBaseAPI sysBaseAPI;

    /**
     * @功能：获取用户菜单
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public Result<JSONObject> getUserMenu(HttpServletRequest req) {
        Integer userId = JwtUtil.getUserIdByToken(req);
        Result<JSONObject> result = new Result<JSONObject>();
        result.setResult(service.getMenuByUser(userId));
        result = result.ok();
        return result;
    }

    /**
     * @功能：获取系统菜单树
     * @return
     */
    @RequestMapping(value = "/tree", method = RequestMethod.POST)
    public Result<JSONObject> queryAllMenuTree(SysMenu sysMenu, HttpServletRequest req){
        Result<JSONObject> result = new Result<JSONObject>();
        result.setResult(service.queryAllMenuTree(sysMenu));
        result.isSuccess();
        return result;
    }

    /**
     * @功能：新增菜单信息
     * @param rowData
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    @AutoLog(value = "添加菜单信息", operateType = SysConfigConstant.OPERATE_TYPE_ADD)
    public Result<SysMenu> add(SysMenu rowData,HttpServletRequest req) {
        rowData.setMenuUesrid(JwtUtil.getUserIdByToken(req));
        return service.add(rowData);
    }

    /**
     * @功能：编辑菜单信息
     * @param rowData
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT)
    @AutoLog(value = "修改菜单信息", operateType = SysConfigConstant.OPERATE_TYPE_UPDATE)
    public Result<SysMenu> edit(SysMenu rowData) {
        return ControllerHelper.edit(rowData, service);
    }

    /**
     * @功能：删除菜单信息
     * @param rowData
     * @return
     */
    @RequestMapping(value = "/del", method = RequestMethod.POST)
    @AutoLog(value = "删除菜单信息", operateType = SysConfigConstant.OPERATE_TYPE_DELETE)
    public Result<SysMenu> delete(SysMenu rowData) {
        return service.delete(rowData);
    }

    /**
     * @功能：获取某一条参数数据
     * @param id 参数代码
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Result<SysMenu> getParamItem(@PathVariable String id) {
        return ControllerHelper.getResultByPk(id, service);
    }


}
