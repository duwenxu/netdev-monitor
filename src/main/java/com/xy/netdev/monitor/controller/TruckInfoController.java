package com.xy.netdev.monitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xy.common.helper.ControllerHelper;
import com.xy.common.helper.ControllerResultWrapper;
import com.xy.common.model.Result;
import com.xy.netdev.common.util.JwtUtil;
import com.xy.netdev.container.BaseContainerLoader;
import com.xy.netdev.monitor.bo.TransUiData;
import com.xy.netdev.monitor.entity.TruckInfo;
import com.xy.netdev.monitor.service.ITruckInfoService;
import com.xy.netdev.transit.IDevCmdSendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 设备车信息 前端控制器
 *
 * @author admin
 * @date 2021-03-05
 */
@Api(value = "设备车信息控制器", tags = "设备车信息")
@RestController
@RequestMapping("/monitor/truckInfo")
public class TruckInfoController {


    @Autowired
    private ITruckInfoService targetService;
    @Autowired
    private BaseContainerLoader BaseInfo;

    /**
     * 获取分页数据
     *
     * @return
     */
    @ApiOperation(value = "获取分页通讯车信息", notes = "获取分页通讯车信息")
    @PostMapping(value = "/list")
    public Result<IPage<TruckInfo>> queryPageList(TruckInfo data, Page page, HttpServletRequest req){
        return ControllerHelper.queryPageList(data, page, req, targetService);
    }

    /**
     * 获取分页数据
     *
     * @return
     */
    @ApiOperation(value = "获取所有通讯车信息", notes = "获取所有通讯车信息")
    @PostMapping(value = "/allList")
    public Result<List<TruckInfo>> queryList(TruckInfo data,HttpServletRequest req){
        return ControllerHelper.queryList(data, req, targetService);
    }


    /**
     * 根据ID查找数据
     */
    @ApiOperation(value = "根据ID查找通讯车信息", notes = "根据ID查找通讯车信息")
    @GetMapping("/{id}")
    public Result<TruckInfo> queryItem(@PathVariable String id){
        TruckInfo entity = targetService.getById(id);
        Result result = new Result();
        result.ok();
        result.setResult(entity);
        return result;
    }

    /**
     * 添加数据
     * @return
     */
    @ApiOperation(value = "添加通讯车信息", notes = "添加通讯车信息")
    @PostMapping
    public Result<TruckInfo> add(TruckInfo data,HttpServletRequest req) {
        Integer userId = JwtUtil.getUserIdByToken(req);
        return ControllerHelper.add(data, targetService);
    }

    /**
     * 更新数据
     * @return
     */
    @ApiOperation(value = "更新通讯车信息", notes = "更新通讯车信息")
    @PutMapping
    public Result<TruckInfo> edit(TruckInfo data) {
        return ControllerHelper.edit(data,targetService);
    }

    /**
     * 删除数据
     * @return
     */
    @ApiOperation(value = "删除通讯车信息", notes = "删除通讯车信息")
    @DeleteMapping("/{id}")
    public Result<TruckInfo> delete(@PathVariable String id) {
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
