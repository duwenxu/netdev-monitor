package com.xy.netdev.admin.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xy.common.model.Result;
import com.xy.common.query.QueryGenerator;
import com.xy.common.util.ConvertUtils;
import com.xy.netdev.admin.service.ISysLogService;
import com.xy.netdev.admin.entity.SysLog;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 系统日志表 前端控制器
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-17
 */
@Api(tags="日志管理接口")
@RestController
@RequestMapping("/admin/sys-log")
public class SysLogController {


    @Autowired
    private ISysLogService sysLogService;

    /**
     * @功能：查询日志记录
     * @param syslog
     * @param page
     * @param req
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<IPage<SysLog>> queryPageList(SysLog syslog, @RequestBody Page page, HttpServletRequest req) {
        Result<IPage<SysLog>> result = new Result<IPage<SysLog>>();
        QueryWrapper<SysLog> queryWrapper = QueryGenerator.initQueryWrapper(syslog, req.getParameterMap());
        //日志关键词
        String keyWord = req.getParameter("keyWord");
        if(ConvertUtils.isNotEmpty(keyWord)) {
            queryWrapper.like("log_content",keyWord);
        }
        //创建时间/创建人的赋值
        IPage<SysLog> pageList = sysLogService.page(page, queryWrapper);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }

    /**
     * @功能：删除单个日志记录
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result<SysLog> delete(@RequestParam(name="id",required=true) String id) {
        Result<SysLog> result = new Result<SysLog>();
        SysLog sysLog = sysLogService.getById(id);
        if(sysLog==null) {
            result.error500("未找到对应实体");
        }else {
            boolean ok = sysLogService.removeById(id);
            if(ok) {
                result.success("删除成功!");
            }
        }
        return result;
    }

}
