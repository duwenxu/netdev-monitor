package com.xy.netdev.monitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.netdev.monitor.entity.ParaInfo;

import javax.servlet.http.HttpServletRequest;

/**
 * 设备参数 服务类
 *
 * @author admin
 * @date 2021-03-05
 */
public interface IParaInfoService extends IService<ParaInfo> {

    /**
     * 获取所有 非子参数 参数分页
     * @param page
     * @param req
     * @param paraInfo
     * @return
     */
    IPage<ParaInfo> queryPageListAll(IPage<ParaInfo> page, HttpServletRequest req, ParaInfo paraInfo);


    IPage<ParaInfo> querySubPageList(IPage<ParaInfo> page, HttpServletRequest req, ParaInfo paraInfo);
}
