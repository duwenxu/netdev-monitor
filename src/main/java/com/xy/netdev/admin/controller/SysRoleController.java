package com.xy.netdev.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xy.common.helper.ControllerHelper;
import com.xy.common.model.Result;
import com.xy.common.util.DateUtils;
import com.xy.netdev.admin.api.ISysBaseAPI;
import com.xy.netdev.admin.entity.SysRole;
import com.xy.netdev.admin.service.ISysMenuService;
import com.xy.netdev.admin.service.ISysRoleService;
import com.xy.netdev.admin.vo.SysRoleMenuModel;
import com.xy.netdev.common.annotation.AutoLog;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.JwtUtil;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 角色信息 前端控制器
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
@Api(tags="角色管理接口")
@RestController
@RequestMapping("/admin/sys-role")
public class SysRoleController {


    @Autowired
    private ISysRoleService service;
    @Autowired
    private ISysBaseAPI sysBaseAPI;
    @Autowired
    ISysMenuService sysMenuService;


    /**
     * @功能：查询用户角色列表
     * @param searchObj
     * @param page
     * @param req
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public Result<IPage<SysRole>> queryPageList(SysRole searchObj, Page page, HttpServletRequest req) {
        return ControllerHelper.queryPageList(searchObj, page, req, service);
    }

    /**
     * @功能：查询所有角色列表
     * @param searchObj
     * @param req
     * @return
     */
    @RequestMapping(value = "/allList", method = RequestMethod.POST)
    public Result<List<SysRole>> queryPageList(SysRole searchObj,HttpServletRequest req) {
        return ControllerHelper.queryList(searchObj, req, service);
    }

    /**
     * @功能：新增角色信息
     * @param rowData
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    @AutoLog(value = "添加角色信息", operateType = SysConfigConstant.OPERATE_TYPE_ADD)
    public Result<SysRole> add(SysRole rowData,HttpServletRequest req) {
        rowData.setRoleStatus(SysConfigConstant.STATUS_OK);
        rowData.setRoleDate(DateUtils.now());
        rowData.setRoleUesrid(JwtUtil.getUserIdByToken(req));
        return ControllerHelper.add(rowData, service);
    }

    /**
     * @功能：编辑角色信息
     * @param rowData
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT)
    @AutoLog(value = "修改角色信息", operateType = SysConfigConstant.OPERATE_TYPE_UPDATE)
    public Result<SysRole> edit(SysRole rowData) {
        return ControllerHelper.edit(rowData, service);
    }

    /**
     * @功能：删除角色信息
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @AutoLog(value = "删除角色信息", operateType = SysConfigConstant.OPERATE_TYPE_DELETE)
    public Result<SysRole> delete(@PathVariable String id) {
        return ControllerHelper.delete(id, service);
    }

    /**
     * @功能：获取某一条参数数据
     * @param id 参数代码
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Result<SysRole> getParamItem(@PathVariable String id) {
        return ControllerHelper.getResultByPk(id, service);
    }


    /**
     * @功能：查询角色拥有权限
     * @return
     */
    @RequestMapping(value = "/menu", method = RequestMethod.POST)
    public Result<Map<String,Object>> queryMenuTreeByRole(SysRole searchObj, HttpServletRequest req){
        Result<Map<String,Object>> result = new Result<Map<String,Object>>();
        result.setResult(sysMenuService.queryMenuTree(searchObj));
        result.isSuccess();
        return result;
    }

    /**
     * @功能：更新角色关联菜单
     * @param roleMenu
     * @return
     */
    @RequestMapping(value = "/updMenu",method = RequestMethod.POST)
    @AutoLog(value="更新角色关联菜单",operateType= SysConfigConstant.OPERATE_TYPE_ADD)
    public Result saveRoleMenus(@RequestBody SysRoleMenuModel roleMenu){
        Result result = new Result();
        result.setResult(service.updateRoleMenus(roleMenu));
        result.isSuccess();
        return result;
    }

}
