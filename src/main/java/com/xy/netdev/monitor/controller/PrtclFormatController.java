package com.xy.netdev.monitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xy.common.model.Result;
import com.xy.common.helper.ControllerHelper;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.monitor.service.IPrtclFormatService;
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
 * 协议格式 前端控制器
 *
 * @author admin
 * @date 2021-03-05
 */
@Api(value = "协议格式", tags = "协议格式")
@RestController
@RequestMapping("/monitor/prtclFormat")
public class PrtclFormatController {

    @Autowired
    private IPrtclFormatService targetService;

    /**
    * 获取分页数据
    *
    * @return
    */
    @ApiOperation(value = "获取分页协议格式", notes = "获取分页协议格式")
    @PostMapping(value = "/list")
    public Result<IPage<PrtclFormat>> queryPageList(PrtclFormat data,Page page,HttpServletRequest req){
        return ControllerHelper.queryPageList(data, page, req, targetService);
    }

    /**
    * 获取分页数据
    *
    * @return
    */
    @ApiOperation(value = "获取所有协议格式", notes = "获取所有协议格式")
    @PostMapping(value = "/allList")
    public Result<List<PrtclFormat>> queryList(PrtclFormat data,HttpServletRequest req){
        return ControllerHelper.queryList(data, req, targetService);
    }


    /**
     * 根据ID查找数据
     */
    @ApiOperation(value = "根据ID查找协议格式", notes = "根据ID查找协议格式")
    @GetMapping("/{id}")
    public Result<PrtclFormat> queryItem(@PathVariable String id){
        PrtclFormat entity = targetService.getById(id);
        return Result.ok().data(entity);
    }

    /**
    * 添加数据
    * @return
    */
    @ApiOperation(value = "添加协议格式", notes = "添加协议格式")
    @PostMapping
    public Result<PrtclFormat> add(PrtclFormat data,HttpServletRequest req) {
        Integer userId = JwtUtil.getUserIdByToken(req);
        return ControllerHelper.add(data, targetService);
    }

    /**
    * 更新数据
    * @return
    */
    @ApiOperation(value = "更新协议格式", notes = "更新协议格式")
    @PutMapping
    public Result<PrtclFormat> edit(PrtclFormat data) {
        return ControllerHelper.edit(data,targetService);
    }

    /**
    * 删除数据
    * @return
    */
    @ApiOperation(value = "删除协议格式", notes = "删除协议格式")
    @DeleteMapping("/{id}")
    public Result<PrtclFormat> delete(@PathVariable String id) {
        return ControllerHelper.delete(id,targetService);
    }

}
