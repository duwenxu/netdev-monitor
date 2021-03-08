package com.xy.netdev.admin.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xy.common.helper.ControllerHelper;
import com.xy.common.model.Result;
import com.xy.common.util.DateUtils;
import com.xy.netdev.admin.api.ISysBaseAPI;
import com.xy.netdev.admin.entity.SysDepart;
import com.xy.netdev.admin.service.ISysDepartService;
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
import java.util.List;

/**
 * <p>
 * 部门信息 前端控制器
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
@Api(tags="部门管理接口")
@RestController
@RequestMapping("/admin/sys-depart")
public class SysDepartController {

    @Autowired
    private ISysDepartService service;
    @Autowired
    private ISysBaseAPI sysBaseAPI;

    /**
     * @功能：查询部门分页列表信息
     * @param searchObj
     * @param page
     * @param req
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public Result<IPage<SysDepart>> queryPageList(SysDepart searchObj, Page page, HttpServletRequest req) {
        return ControllerHelper.queryPageList(searchObj, page, req, service);
    }

    /**
     * @功能：查询所有部门信息
     * @param searchObj
     * @param req
     * @return
     */
    @RequestMapping(value = "/allList", method = RequestMethod.POST)
    public Result<List<SysDepart>> queryPageList(SysDepart searchObj, HttpServletRequest req) {
        return ControllerHelper.queryList(searchObj, req, service);
    }

    /**
     * @功能：新增部门信息
     * @param rowData
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    @AutoLog(value = "添加用户所属部门信息", operateType = SysConfigConstant.OPERATE_TYPE_ADD)
    public Result<SysDepart> add(SysDepart rowData, HttpServletRequest req) {
        Integer userId = JwtUtil.getUserIdByToken(req);
        rowData.setDepartStatus(SysConfigConstant.STATUS_OK);
        rowData.setDepartDate(DateUtils.now());
        rowData.setDepartUesrid(userId);
        return ControllerHelper.add(rowData, service);
    }

    /**
     * @功能：编辑部门信息
     * @param rowData
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT)
    @AutoLog(value = "修改部门信息", operateType = SysConfigConstant.OPERATE_TYPE_UPDATE)
    public Result<SysDepart> edit(SysDepart rowData) {
        return ControllerHelper.edit(rowData, service);
    }

    /**
     * @功能：删除部门信息
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @AutoLog(value = "删除部门信息", operateType = SysConfigConstant.OPERATE_TYPE_DELETE)
    public Result<SysDepart> delete(@PathVariable String id) {
        return ControllerHelper.delete(id, service);
    }

    /**
     * @功能：获取某一条参数数据
     * @param id 参数代码
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Result<SysDepart> getParamItem(@PathVariable String id) {
        return ControllerHelper.getResultByPk(id, service);
    }

}
