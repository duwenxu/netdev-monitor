package com.xy.netdev.container;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.monitor.service.IBaseInfoService;
import com.xy.netdev.monitor.service.IInterfaceService;
import com.xy.netdev.monitor.service.IParaInfoService;
import com.xy.netdev.monitor.service.IPrtclFormatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *基础信息容器装载类
 * </p>
 *
 * @author sunchao
 * @since 2021-03-09
 */
@Slf4j
@Component
public class BaseContainerLoader {

    @Autowired
    private IBaseInfoService baseInfoService;
    @Autowired
    private IParaInfoService paraInfoService;
    @Autowired
    private IInterfaceService interfaceService;
    @Autowired
    private IPrtclFormatService prtclFormatService;
    @Autowired
    private ISysParamService sysParamService;

    /**
     * 初始化信息加载
     */
    @PostConstruct
    public void load(){
        log.info("开始更新容器信息！");
        long time = System.currentTimeMillis();
        //初始化基础信息
        initBaseInfo();
        //初始化设备容器
        //initDevInfo();
        //初始化设备日志容器
        initDevLog();
        //初始化告警信息
        initDevAlert();
        //初始化设备参数容器
        initDevParam();
        //初始化设备状态容器
        DevStatusContainer.init(sysParamService);
        log.info("容器信息更新完成，耗时:["+(System.currentTimeMillis()-time)+"ms]");
    }

    /**
     * 加载基础信息容器
     */
    public void initBaseInfo(){
        //查询有效的设备列表
        List<BaseInfo> devs = baseInfoService.list().stream().filter(baseInfo -> baseInfo.getDevStatus().equals(SysConfigConstant.DEV_STATUS_NEW)).collect(Collectors.toList());
        //查询有效的参数列表
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("NDPA_STATUS",SysConfigConstant.STATUS_OK);
        queryWrapper.orderByAsc("NDPA_CMPLEX_LEVEL");
        List<ParaInfo> paraInfos = paraInfoService.list(queryWrapper);
        paraInfos.forEach(paraInfo -> {
            paraInfo.setDevTypeCode(sysParamService.getParaRemark1(paraInfo.getDevType()));
        });
        //查询有效的接口列表
        List<Interface> interfaces = interfaceService.list().stream().filter(anInterface -> anInterface.getItfStatus().equals(SysConfigConstant.STATUS_OK)).collect(Collectors.toList());
        //查询协议列表
        List<PrtclFormat> prtclList = prtclFormatService.list();
        //初始化基础容器的数据
        BaseInfoContainer.init(devs,paraInfos,interfaces,prtclList);
    }

    /**
     * 加载设备参数信息
     */
    private void initDevParam(){
        //查询有效的参数列表:根据NDPA_CMPLEX_LEVEL对list：用来生成参数的上下级关系
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("NDPA_STATUS",SysConfigConstant.STATUS_OK);
        queryWrapper.orderByAsc("NDPA_CMPLEX_LEVEL");
        List<ParaInfo> paraInfos = paraInfoService.list(queryWrapper);
        DevParaInfoContainer.initData(paraInfos,sysParamService);
    }

    /**
     * 加载设备日志容器
     */
    private void initDevLog(){
        int devLogSize = Integer.parseInt(sysParamService.getParaRemark1(SysConfigConstant.DEV_LOG_VIEW_SZIE));
        //初始化各设备日志
        DevLogInfoContainer.init(devLogSize);
    }

    /**
     * 加载设备日志容器
     */
    private void initDevAlert(){
        int devAlertInfoSize = Integer.parseInt(sysParamService.getParaRemark1(SysConfigConstant.DEV_ALERT_INFO_SZIE));
        //初始化各设备日志
        DevAlertInfoContainer.init(devAlertInfoSize);
    }
}
