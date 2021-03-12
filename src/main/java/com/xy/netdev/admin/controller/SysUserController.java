package com.xy.netdev.admin.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xy.common.helper.ControllerHelper;
import com.xy.common.helper.ControllerResultWrapper;
import com.xy.common.model.Result;
import com.xy.common.query.QueryGenerator;
import com.xy.netdev.admin.entity.SysUser;
import com.xy.netdev.admin.service.ISysUserService;
import com.xy.netdev.common.annotation.AutoLog;
import com.xy.netdev.common.constant.SysConfigConstant;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 用户信息 前端控制器
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
@Api(tags="系统用户管理接口")
@RestController
@Slf4j
@RequestMapping("/admin/sys-user")
public class SysUserController {

    @Autowired
    private ISysUserService sysUserService;


    /**
     * @功能：查询用户列表
     * @param user
     * @param page
     * @param req
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public Result<IPage<SysUser>> queryPageList(SysUser user, Page page, HttpServletRequest req) {
        QueryWrapper<SysUser> queryWrapper = QueryGenerator.initQueryWrapper(user, req.getParameterMap());
        IPage<SysUser> rst = sysUserService.queryUserPageList(page, queryWrapper);
        return ControllerResultWrapper.genPageListResult(rst);
    }

    /**
     * @功能：查询用户列表
     * @param user
     * @param req
     * @return
     */
    @RequestMapping(value = "/alllist", method = RequestMethod.POST)
    public Result<List<SysUser>> queryPageList(SysUser user,HttpServletRequest req) {
        return ControllerHelper.queryList(user, req, sysUserService);
    }

    /**
     * @功能：添加用户信息
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    public Result<SysUser> add(@RequestBody SysUser user) {
        Result<SysUser> result = new Result<SysUser>();
        try {
            result = sysUserService.addUser(user);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败");
        }
        return result;
    }

    /**
     * @功能：修改用户信息
     * @param user
     * @return
     */
    @RequestMapping( method = RequestMethod.PUT)
    @AutoLog(value="修改用户信息",operateType= SysConfigConstant.OPERATE_TYPE_UPDATE)
    public Result<SysUser> edit(@RequestBody  SysUser user) {
        Result<SysUser> result = new Result<SysUser>();
        try {
            result = sysUserService.editUser(user);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("操作失败");
        }
        return result;
    }

    /**
     * @功能：删除用户信息
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @AutoLog(value="删除用户信息",operateType= SysConfigConstant.OPERATE_TYPE_DELETE)
    public Result<SysUser> delete(@PathVariable String id) {
        return ControllerHelper.delete(id,sysUserService);
    }

    /**
     * @功能：根据用户id查询用户信息
     * @param id 调度执行登记ID
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Result<SysUser> getUserItem(@PathVariable String id) {
        return ControllerHelper.getResultByPk(id,sysUserService);
    }


}
