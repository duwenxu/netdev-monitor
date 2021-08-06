package com.xy.netdev.WorkPlan.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xy.common.helper.ControllerHelper;
import com.xy.common.model.Result;
import com.xy.common.util.DateUtils;
import com.xy.netdev.WorkPlan.entity.NtdvWorkPlan;
import com.xy.netdev.WorkPlan.service.INtdvWorkPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 值班安排 前端控制器
 * </p>
 *
 * @author zb
 * @since 2021-06-09
 */
@Api(value = "值班安排", tags = "值班安排")
@RestController
@RequestMapping("/WorkPlan/ntdv-work-plan")
public class NtdvWorkPlanController {
    @Autowired
    private INtdvWorkPlanService workPlanService;


    /**
     * 获取分页数据
     *
     * @return
     */
    @ApiOperation(value = "获取分页值班格式", notes = "获取分页值班格式")
    @PostMapping(value = "/list")
    public Result<IPage<NtdvWorkPlan>> queryPageList(NtdvWorkPlan workPlan, Page page, HttpServletRequest req) {
        // 按当前时间去筛选结束时间之后的值班计划
        workPlan.setWpEndTime("> " + DateUtils.now());
        return ControllerHelper.queryPageList(workPlan, page, req, workPlanService);
    }

    /**
     * 添加数据
     *
     * @return
     */
    @ApiOperation(value = "添加值班安排", notes = "添加值班安排")
    @PostMapping
    public Result<NtdvWorkPlan> add(NtdvWorkPlan plan) {
        return ControllerHelper.add(plan, workPlanService);
    }

    /**
     * 更新数据
     *
     * @return
     */
    @ApiOperation(value = "更新值班安排", notes = "更新值班安排")
    @PutMapping
    public Result<NtdvWorkPlan> edit(NtdvWorkPlan plan) {
        return ControllerHelper.edit(plan, workPlanService);
    }

    /**
     * 删除数据
     *
     * @return
     */
    @ApiOperation(value = "删除值班安排", notes = "删除值班安排")
    @DeleteMapping("/{id}")
    public Result<NtdvWorkPlan> delete(@PathVariable String id) {
        return ControllerHelper.delete(id, workPlanService);
    }

}
