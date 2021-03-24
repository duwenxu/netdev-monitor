package com.xy.netdev.monitor.controller;

import cn.hutool.core.thread.ThreadUtil;
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
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

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
    private IBaseInfoService baseInfoService;

    /**
    * 获取分页数据
    *
    * @return
    */
    @ApiOperation(value = "获取分页设备信息", notes = "获取分页设备信息")
    @PostMapping(value = "/list")
    public Result<IPage<BaseInfo>> queryPageList(BaseInfo data,Page page,HttpServletRequest req){
        return ControllerHelper.queryPageList(data, page, req, baseInfoService);
    }

    /**
    * 获取分页数据
    *
    * @return
    */
    @ApiOperation(value = "获取所有设备信息", notes = "获取所有设备信息")
    @PostMapping(value = "/allList")
    public Result<List<BaseInfo>> queryList(BaseInfo data,HttpServletRequest req){
        return ControllerHelper.queryList(data, req, baseInfoService);
    }


    /**
     * 根据ID查找数据
     */
    @ApiOperation(value = "根据ID查找设备信息", notes = "根据ID查找设备信息")
    @GetMapping("/{id}")
    public Result<BaseInfo> queryItem(@PathVariable String id){
        BaseInfo entity = baseInfoService.getById(id);
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
        Result<BaseInfo> result = ControllerHelper.add(data, baseInfoService);
        ThreadUtil.execAsync(() ->baseInfoService.devStatusUpdate(data.getDevNo(), null));
        return result;
    }

    /**
    * 更新数据
    * @return
    */
    @ApiOperation(value = "更新设备信息", notes = "更新设备信息")
    @PutMapping
    public Result<BaseInfo> edit(BaseInfo data) {
        Result<BaseInfo> result = ControllerHelper.edit(data, baseInfoService);
        //异步执行通知
        ThreadUtil.execAsync(() ->baseInfoService.devStatusUpdate(data.getDevNo(), null));
        return result;
    }

    /**
    * 删除数据
    * @return
    */
    @ApiOperation(value = "删除设备信息", notes = "删除设备信息")
    @DeleteMapping("/{id}")
    public Result<BaseInfo> delete(@PathVariable String id) {
        String devParentNo = this.baseInfoService.getById(id).getDevParentNo();
        Result<BaseInfo> result = ControllerHelper.delete(id, baseInfoService);
        ThreadUtil.execAsync(() ->baseInfoService.devStatusUpdate(null, devParentNo));
        return result;
    }

    /**
     * 设备导航信息
     * @return
     */
    @ApiOperation(value = "设备信息导航", notes = "设备信息导航")
    @GetMapping("/baseMenu")
    public Result<Object> baseMenu() {
        Map<String, Object> map = baseInfoService.baseInfoMenuMap();
        Result<Object> result = new Result<>();
        result.setResult(map);
        return result;
    }

    /**
     * 切换当前正在使用的设备
     * @return 请求结果
     */
    @ApiOperation(value = "切换主设备", notes = "切换主设备")
    @GetMapping("/changeMaster")
    public Result<Object> changeUseStatus(String devNo) {
        boolean isOk = baseInfoService.changeUseStatus(devNo);
        if (isOk){
            return new Result<>().ok();
        }else {
            return new Result<>().error500("主备切换失败");
        }
    }

    /**
     * 设备模型文件下载
     * @return
     */
    @ApiOperation(value = "设备模型文件下载", notes = "设备模型文件下载")
    @PostMapping("/downDevFile")
    public Result<Object> downDevFile(HttpServletResponse response) {
        Map<String, Object> map = baseInfoService.downDevFile();
        //浏览器下载excel
        response.addHeader("fileName",map.get("fileName").toString());
        //解决前端拿不到存放的文件参数:将新增的两个参数暴露出来
        response.setHeader("Access-Control-Expose-Headers","fileName");
        response.setContentType("application/octet-stream");
        try {
            response.flushBuffer();
            OutputStream outputStream = response.getOutputStream();
            outputStream.write((byte[])map.get("fileContext"));
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            return new Result<>().error500(e.getMessage());
        }
        return null;
    }

}
