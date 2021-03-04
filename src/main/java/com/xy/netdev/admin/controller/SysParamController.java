package com.xy.netdev.admin.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xy.common.helper.ControllerHelper;
import com.xy.common.helper.ControllerResultWrapper;
import com.xy.common.model.Result;
import com.xy.netdev.admin.entity.SysParam;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.annotation.AutoLog;
import com.xy.netdev.common.constant.SysConfigConstant;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 参数信息 前端控制器
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
@Api(tags = "参数管理接口")
@RestController
@RequestMapping("/admin/sys-param")
public class SysParamController {

    @Autowired
    private ISysParamService service;

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public Result<IPage<SysParam>> queryPageList(SysParam searchObj, Page page, HttpServletRequest req) {
        return ControllerHelper.queryPageList(searchObj, page, req, service);
    }

    /**
     * @param rowData
     * @return
     * @功能：新增
     */
    @RequestMapping(method = RequestMethod.POST)
    @AutoLog(value = "添加系统参数", operateType = SysConfigConstant.OPERATE_TYPE_ADD)
    public Result<SysParam> add(SysParam rowData) {
        rowData.setIsValidate(SysConfigConstant.STATUS_OK);
        return ControllerHelper.add(rowData, service);
    }

    /**
     * @功能：编辑
     * @param sysParam
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT)
    @AutoLog(value="修改系统参数",operateType= SysConfigConstant.OPERATE_TYPE_UPDATE)
    public Result<SysParam> edit(SysParam sysParam) {
        Result<SysParam> result = new Result<SysParam>();
        SysParam sysparam = service.getById(sysParam.getParaCode());
        if(sysparam==null) {
            result.error500("未找到对应实体");
        }else {
            boolean ok = service.updateById(sysParam);
            if(ok) {
                //编辑成功后清理参数缓存
                service.deleteCacheComboxData(sysParam.getParaCode());
                result.success("编辑成功!");
            }
        }
        return result;
    }


    /**
     * @功能：删除
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @AutoLog(value="删除系统参数",operateType= SysConfigConstant.OPERATE_TYPE_DELETE)
    public Result<SysParam> delete(@PathVariable String id) {
        String paraCode = id;
        Result<SysParam> result = new Result<SysParam>();
        boolean ok = service.removeById(paraCode);
        if(ok) {
            //删除成功后清理参数缓存
            service.deleteCacheComboxData(paraCode);
            result.success("删除成功!");
        }else{
            result.error500("删除失败!");
        }
        return result;
    }



    /**
     * 获取某一条参数数据
     *
     * @param id 参数代码
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Result<SysParam> getParamItem(@PathVariable String id) {
        return ControllerHelper.getResultByPk(id, service);
    }

    /**
     * 获取某父ID 下的所有该子项参数数据
     *
     * @param id 参数代码
     * @return
     */
    @GetMapping(value = "/queryParamByParentId/{id}")
    public Result<List<SysParam>> queryParamByParentId(@PathVariable String id) {
        Result<List<SysParam>> result = new Result<List<SysParam>>();
        return ControllerResultWrapper.genListResult(service.queryParamsByParentId(id));
    }

}
