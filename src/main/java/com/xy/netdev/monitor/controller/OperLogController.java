package com.xy.netdev.monitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xy.common.model.Result;
import com.xy.common.helper.ControllerHelper;
import com.xy.netdev.monitor.entity.OperLog;
import com.xy.netdev.monitor.service.IOperLogService;
import com.xy.netdev.common.annotation.AutoLog;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 操作日志信息 前端控制器
 *
 * @author admin
 * @date 2021-03-05
 */
@Api(value = "操作日志信息", tags = "操作日志信息")
@RestController
@RequestMapping("/monitor/operLog")
public class OperLogController {

    @Autowired
    private IOperLogService targetService;

    /**
    * 获取分页数据
    *
    * @return
    */
    @ApiOperation(value = "获取分页操作日志信息", notes = "获取分页操作日志信息")
    @PostMapping(value = "/list")
    public Result<IPage<OperLog>> queryPageList(OperLog data,Page page,HttpServletRequest req){
        return ControllerHelper.queryPageList(data, page, req, targetService);
    }

    /**
    * 获取分页数据
    *
    * @return
    */
    @ApiOperation(value = "获取所有操作日志信息", notes = "获取所有操作日志信息")
    @PostMapping(value = "/allList")
    public Result<List<OperLog>> queryList(OperLog data,HttpServletRequest req){
        return ControllerHelper.queryList(data, req, targetService);
    }


    /**
     * 根据ID查找数据
     */
    @ApiOperation(value = "根据ID查找操作日志信息", notes = "根据ID查找操作日志信息")
    @GetMapping("/{id}")
    public Result<OperLog> queryItem(@PathVariable String id){
        OperLog entity = targetService.getById(id);
        Result result = new Result();
        result.ok();
        result.setResult(entity);
        return result;
    }

    /**
    * 添加数据
    * @return
    */
    @ApiOperation(value = "添加操作日志信息", notes = "添加操作日志信息")
    @PostMapping
    public Result<OperLog> add(OperLog data,HttpServletRequest req) {
        Integer userId = JwtUtil.getUserIdByToken(req);
        return ControllerHelper.add(data, targetService);
    }

    /**
    * 更新数据
    * @return
    */
    @ApiOperation(value = "更新操作日志信息", notes = "更新操作日志信息")
    @PutMapping
    public Result<OperLog> edit(OperLog data) {
        return ControllerHelper.edit(data,targetService);
    }

    /**
    * 删除数据
    * @return
    */
    @ApiOperation(value = "删除操作日志信息", notes = "删除操作日志信息")
    @DeleteMapping("/{id}")
    public Result<OperLog> delete(@PathVariable String id) {
        return ControllerHelper.delete(id,targetService);
    }

}
