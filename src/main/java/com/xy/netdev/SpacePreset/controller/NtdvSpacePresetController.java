package com.xy.netdev.SpacePreset.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xy.common.helper.ControllerHelper;
import com.xy.common.model.Result;
import com.xy.netdev.SpacePreset.entity.NtdvSpacePreset;
import com.xy.netdev.SpacePreset.service.INtdvSpacePresetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 卫星预置信息 前端控制器
 * </p>
 *
 * @author zb
 * @since 2021-06-09
 */
@Api(value = "卫星预置", tags = "卫星预置")
@RestController
@RequestMapping("/SpacePreset/ntdv-space-preset")
public class NtdvSpacePresetController {
    @Autowired
    private INtdvSpacePresetService spacePresetService;


    /**
     * 获取分页数据
     *
     * @return
     */
    @ApiOperation(value = "获取卫星预置分页格式", notes = "获取卫星预置分页格式")
    @PostMapping(value = "/list")
    public Result<IPage<NtdvSpacePreset>> queryPageList(NtdvSpacePreset spacePreset, Page page, HttpServletRequest req) {
        return ControllerHelper.queryPageList(spacePreset, page, req, spacePresetService);
    }

    /**
     * 添加数据
     *
     * @return
     */
    @ApiOperation(value = "添加卫星预置信息", notes = "添加卫星预置信息")
    @PostMapping
    public Result<NtdvSpacePreset> add(NtdvSpacePreset spacePreset) {
        return ControllerHelper.add(spacePreset, spacePresetService);
    }

    /**
     * 更新数据
     *
     * @return
     */
    @ApiOperation(value = "更新卫星预置信息", notes = "更新卫星预置信息")
    @PutMapping
    public Result<NtdvSpacePreset> edit(NtdvSpacePreset spacePreset) {
        return ControllerHelper.edit(spacePreset, spacePresetService);
    }

    /**
     * 删除数据
     *
     * @return
     */
    @ApiOperation(value = "删除卫星预置信息", notes = "删除卫星预置信息")
    @DeleteMapping("/{id}")
    public Result<NtdvSpacePreset> delete(@PathVariable String id) {
        return ControllerHelper.delete(id, spacePresetService);
    }

}
