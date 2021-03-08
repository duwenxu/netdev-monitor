package com.xy.netdev.monitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xy.common.helper.ControllerHelper;
import com.xy.common.model.Result;
import com.xy.netdev.common.util.JwtUtil;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.service.IBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 设备信息 前端控制器
 *
 * @author admin
 * @date 2021-03-05
 */
@Api(value = "设备信息", tags = "设备信息")
@RestController
@RequestMapping("/monitor/baseInfo")
public class BaseInfoController {

    @Autowired
    private IBaseInfoService targetService;

    /**
    * 获取分页数据
    *
    * @return
    */
    @ApiOperation(value = "获取分页设备信息", notes = "获取分页设备信息")
    @PostMapping(value = "/list")
    public Result<IPage<BaseInfo>> queryPageList(BaseInfo data,Page page,HttpServletRequest req){
        return ControllerHelper.queryPageList(data, page, req, targetService);
    }

    /**
    * 获取分页数据
    *
    * @return
    */
    @ApiOperation(value = "获取所有设备信息", notes = "获取所有设备信息")
    @PostMapping(value = "/allList")
    public Result<List<BaseInfo>> queryList(BaseInfo data,HttpServletRequest req){
        return ControllerHelper.queryList(data, req, targetService);
    }


    /**
     * 根据ID查找数据
     */
    @ApiOperation(value = "根据ID查找设备信息", notes = "根据ID查找设备信息")
    @GetMapping("/{id}")
    public Result<BaseInfo> queryItem(@PathVariable String id){
        BaseInfo entity = targetService.getById(id);
        Result result = new Result();
        result.ok();
        result.setResult(entity);
        return result;
    }

    /**
    * 添加数据
    * @return
    */
    @ApiOperation(value = "添加设备信息", notes = "添加设备信息")
    @PostMapping
    public Result<BaseInfo> add(BaseInfo data,HttpServletRequest req) {
        Integer userId = JwtUtil.getUserIdByToken(req);
        return ControllerHelper.add(data, targetService);
    }

    /**
    * 更新数据
    * @return
    */
    @ApiOperation(value = "更新设备信息", notes = "更新设备信息")
    @PutMapping
    public Result<BaseInfo> edit(BaseInfo data) {
        return ControllerHelper.edit(data,targetService);
    }

    /**
    * 删除数据
    * @return
    */
    @ApiOperation(value = "删除设备信息", notes = "删除设备信息")
    @DeleteMapping("/{id}")
    public Result<BaseInfo> delete(@PathVariable String id) {
        return ControllerHelper.delete(id,targetService);
    }

}
