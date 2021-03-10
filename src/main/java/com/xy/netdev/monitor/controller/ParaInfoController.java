package com.xy.netdev.monitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xy.common.helper.ControllerHelper;
import com.xy.common.model.Result;
import com.xy.netdev.common.util.JwtUtil;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.monitor.service.IParaInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 设备参数 前端控制器
 *
 * @author admin
 * @date 2021-03-05
 */
@Api(value = "设备参数", tags = "设备参数")
@RestController
@RequestMapping("/monitor/paraInfo")
public class ParaInfoController {

    @Autowired
    private IParaInfoService targetService;

    /**
    * 获取分页数据
    *
    * @return
    */
    @ApiOperation(value = "获取分页设备参数", notes = "获取分页设备参数")
    @PostMapping(value = "/list")
    public Result<IPage<ParaInfo>> queryPageList(ParaInfo data,Page page,HttpServletRequest req){
        return ControllerHelper.queryPageList(data, page, req, targetService);
    }

    /**
    * 获取分页数据
    *
    * @return
    */
    @ApiOperation(value = "获取所有设备参数", notes = "获取所有设备参数")
    @PostMapping(value = "/allList")
    public Result<List<ParaInfo>> queryList(ParaInfo data,HttpServletRequest req){
        return ControllerHelper.queryList(data, req, targetService);
    }


    /**
     * 根据ID查找数据
     */
    @ApiOperation(value = "根据ID查找设备参数", notes = "根据ID查找设备参数")
    @GetMapping("/{id}")
    public Result<ParaInfo> queryItem(@PathVariable String id){
        ParaInfo entity = targetService.getById(id);
        Result result = new Result();
        result.ok();
        result.setResult(entity);
        return result;
    }

    /**
    * 添加数据
    * @return
    */
    @ApiOperation(value = "添加设备参数", notes = "添加设备参数")
    @PostMapping
    public Result<ParaInfo> add(ParaInfo data,HttpServletRequest req) {
        Integer userId = JwtUtil.getUserIdByToken(req);
        return ControllerHelper.add(data, targetService);
    }

    /**
    * 更新数据
    * @return
    */
    @ApiOperation(value = "更新设备参数", notes = "更新设备参数")
    @PutMapping
    public Result<ParaInfo> edit(ParaInfo data) {
        return ControllerHelper.edit(data,targetService);
    }

    /**
    * 删除数据
    * @return
    */
    @ApiOperation(value = "删除设备参数", notes = "删除设备参数")
    @DeleteMapping("/{id}")
    public Result<ParaInfo> delete(@PathVariable String id) {
        return ControllerHelper.delete(id,targetService);
    }

}
