package com.xy.netdev.monitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xy.common.helper.ControllerResultWrapper;
import com.xy.common.model.Result;
import com.xy.common.helper.ControllerHelper;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.service.IInterfaceService;
import com.xy.netdev.common.util.JwtUtil;
import com.xy.netdev.monitor.vo.TransUiData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 设备接口 前端控制器
 *
 * @author admin
 * @date 2021-03-05
 */
@Api(value = "设备接口", tags = "设备接口")
@RestController
@RequestMapping("/monitor/interface")
public class InterfaceController {

    @Autowired
    private IInterfaceService targetService;

    /**
    * 获取分页数据
    *
    * @return
    */
    @ApiOperation(value = "获取分页设备接口", notes = "获取分页设备接口")
    @PostMapping(value = "/list")
    public Result<IPage<Interface>> queryPageList(Interface data,Page page,HttpServletRequest req){
        return ControllerHelper.queryPageList(data, page, req, targetService);
    }

    /**
    * 获取分页数据
    *
    * @return
    */
    @ApiOperation(value = "获取所有设备接口", notes = "获取所有设备接口")
    @PostMapping(value = "/allList")
    public Result<List<Interface>> queryList(Interface data,HttpServletRequest req){
        return ControllerHelper.queryList(data, req, targetService);
    }


    /**
     * 根据ID查找数据
     */
    @ApiOperation(value = "根据ID查找设备接口", notes = "根据ID查找设备接口")
    @GetMapping("/{id}")
    public Result<Interface> queryItem(@PathVariable String id){
        Interface entity = targetService.getById(id);
        Result result = new Result();
        result.ok();
        result.setResult(entity);
        return result;
    }

    /**
    * 添加数据
    * @return
    */
    @ApiOperation(value = "添加设备接口", notes = "添加设备接口")
    @PostMapping
    public Result<Interface> add(Interface data,HttpServletRequest req) {
        Integer userId = JwtUtil.getUserIdByToken(req);
        return ControllerHelper.add(data, targetService);
    }

    /**
    * 更新数据
    * @return
    */
    @ApiOperation(value = "更新设备接口", notes = "更新设备接口")
    @PutMapping
    public Result<Interface> edit(Interface data) {
        return ControllerHelper.edit(data,targetService);
    }

    /**
    * 删除数据
    * @return
    */
    @ApiOperation(value = "删除设备接口", notes = "删除设备接口")
    @DeleteMapping("/{id}")
    public Result<Interface> delete(@PathVariable String id) {
        return ControllerHelper.delete(id,targetService);
    }

    @ApiOperation(value = "组件右框已连接事件", notes = "组件右框已连接事件")
    @RequestMapping(value = "/linked/{id}", method = RequestMethod.GET)
    public Result<List<TransUiData>> getMdlLinkedEvents(@PathVariable String id)  {
        return ControllerResultWrapper.genListResult(targetService.getlLinkedParams(id));
    }

    @ApiOperation(value = "组件左框未连接事件", notes = "组件左框未连接事件")
    @RequestMapping(value = "/unlinked/{id}", method = RequestMethod.GET)
    public Result<List<TransUiData>> getMdlUnlinkedEvents(@PathVariable String id)  {
        return ControllerResultWrapper.genListResult(targetService.getUnlinkedParams(id));
    }

}