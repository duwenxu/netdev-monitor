package com.xy.netdev.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.common.query.QueryGenerator;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.monitor.mapper.ParaInfoMapper;
import com.xy.netdev.monitor.service.IParaInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

import static com.xy.netdev.common.constant.SysConfigConstant.PARA_COMPLEX_LEVEL_SUB;

/**
 * 设备参数 服务实现类
 *
 * @author admin
 * @date 2021-03-05
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ParaInfoServiceImpl extends ServiceImpl<ParaInfoMapper, ParaInfo> implements IParaInfoService {


    @Override
    public IPage<ParaInfo> queryPageListAll(IPage<ParaInfo> page, HttpServletRequest req, ParaInfo paraInfo) {
        QueryWrapper<ParaInfo> queryWrapper = QueryGenerator.initQueryWrapper(paraInfo, req.getParameterMap());
        queryWrapper.notIn("NDPA_CMPLEX_LEVEL",PARA_COMPLEX_LEVEL_SUB);
        return this.page(page, queryWrapper);
    }

    @Override
    public IPage<ParaInfo> querySubPageList(IPage<ParaInfo> page, HttpServletRequest req, ParaInfo paraInfo) {
        QueryWrapper<ParaInfo> queryWrapper = QueryGenerator.initQueryWrapper(paraInfo, req.getParameterMap());
        queryWrapper.in("NDPA_CMPLEX_LEVEL",PARA_COMPLEX_LEVEL_SUB);
        return this.page(page, queryWrapper);
    }
}
