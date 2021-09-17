package com.xy.netdev.monitor.controller;

import com.xy.common.helper.ControllerResultWrapper;
import com.xy.common.model.Result;
import com.xy.netdev.common.util.ParaHandlerUtil;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.frame.service.shipAcu.util.AngleUtil;
import com.xy.netdev.monitor.bo.Angel;
import com.xy.netdev.monitor.service.IShipAcuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


/**
 * 船载1.5米ACU 前端控制器
 *
 * @author sunchao
 * @date 2021-05-28
 */
@Api(value = "设备接口", tags = "设备接口")
@RestController
@RequestMapping("/monitor/acuCtrl")
public class ShipAcuController {

    @Autowired
    private IShipAcuService shipAcuService;

    /**
     * 计算地面站对准卫星的角度
     * @return
     */
    @ApiOperation(value = "计算地面站对准卫星的角度", notes = "计算地面站对准卫星的角度")
    @PostMapping(value = "/ctrlAngle")
    public Result<Map<String, String>> ctrlAngle(Angel angel) {
        double satJd = Double.valueOf(angel.getSatJd());
        double devJd = Double.valueOf(angel.getDevJd());
        double devWd = Double.valueOf(angel.getDevWd());
        boolean isLevel  = angel.getIsLevel();
        return ControllerResultWrapper.genGetOneResult(AngleUtil.ctrlAngel(satJd,devJd,devWd,isLevel));
    }

    /**
     * 获取当前位置的经纬度
     * @return
     */
    @ApiOperation(value = "获取当前位置的经纬度", notes = "获取当前位置的经纬度")
    @PostMapping(value = "/getLocalDeg")
    public Result<Angel> getLocalDeg(Angel angel) {
        return ControllerResultWrapper.genGetOneResult(shipAcuService.getLocalDeg(angel));
    }

    /**
     * 手动执行（星下点/空间指向）
     * @return
     */
    @ApiOperation(value = "手动执行", notes = "手动执行")
    @PostMapping(value = "/operCtrl")
    public Result operCtrl(Angel angel) {
        shipAcuService.operCtrl(angel);
        //修改当前工作方式标志位参数值
        DevParaInfoContainer.updateParaValue(angel.getDevNo(), ParaHandlerUtil.genLinkKey(angel.getDevNo(), "73"),"1");
        return ControllerResultWrapper.genAddResult();
    }

    /**
     * 自动执行（星下点/空间指向）
     * @return
     */
    @ApiOperation(value = "自动执行", notes = "手动执行")
    @PostMapping(value = "/autoCtrl")
    public Result autoCtrl(Angel angel) {
        shipAcuService.autoCtrl(angel);
        return ControllerResultWrapper.genAddResult();
    }

    /**
     * 获取当前状态
     * @return
     */
    @ApiOperation(value = "获取当前状态", notes = "获取当前状态")
    @PostMapping(value = "/getCurrentStage")
    public Result<Angel> getCurrentStage(Angel angel) {
        return ControllerResultWrapper.genGetOneResult(shipAcuService.getCurrentStage(angel));
    }
}
