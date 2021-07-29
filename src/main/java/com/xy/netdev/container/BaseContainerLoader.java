package com.xy.netdev.container;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xy.netdev.SpacePreset.service.INtdvSpacePresetService;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.BeanFactoryUtil;
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
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 基础信息容器装载类
 * </p>
 *
 * @author sunchao
 * @since 2021-03-09
 */
@Slf4j
@Order(50)
@Component
public class BaseContainerLoader implements ApplicationRunner {

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
    @Autowired
    private INtdvSpacePresetService spacePresetService;

    //第三类设备-动中通
    private static String DEV_TYPE_DZT = "3";
    /**
     * 初始化信息加载
     */
    @PostConstruct
    public void load() {
        long time = System.currentTimeMillis();
        //初始化基础信息
        initBaseInfo();
        //初始化设备日志容器
        initDevLog();
        //初始化告警信息
        initDevAlert();
        log.info("容器信息更新完成，耗时:[" + (System.currentTimeMillis() - time) + "ms]");
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //初始化设备参数容器
        initDevParam();
        //初始化设备控制接口信息容器
        DevCtrlInterInfoContainer.initData();
        //初始化设备状态容器
        DevStatusContainer.init(sysParamService);
        //初始化SNMP固定设备参数
        DevParaInfoContainer.initSnmpDevStatusRptData();
    }

    /**
     * 加载基础信息容器
     */
    public void initBaseInfo() {
        //查询有效的设备列表
        List<BaseInfo> devs = baseInfoService.list().stream().
                filter(baseInfo -> !baseInfo.getDevStatus().equals(SysConfigConstant.DEV_STATUS_REPAIR))
                .collect(Collectors.toList());
        //查询有效的参数列表:根据NDPA_CMPLEX_LEVEL对list：用来生成参数的上下级关系
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("NDPA_STATUS", SysConfigConstant.STATUS_OK);
        queryWrapper.orderByAsc("NDPA_CMPLEX_LEVEL");
        List<ParaInfo> paraInfos = paraInfoService.list(queryWrapper);
        paraInfos.forEach(paraInfo -> {
            paraInfo.setDevTypeCode(sysParamService.getParaRemark1(paraInfo.getDevType()));
        });
        //查询有效的接口列表
        List<Interface> interfaces = interfaceService.list().stream().
                filter(anInterface -> anInterface.getItfStatus().equals(SysConfigConstant.STATUS_OK))
                .collect(Collectors.toList());
        //查询协议列表
        List<PrtclFormat> prtclList = prtclFormatService.list();
        //初始化基础容器的数据
        BaseInfoContainer.init(devs, paraInfos, interfaces, prtclList,spacePresetService.list());
    }

    /**
     * 加载设备参数信息
     */
    private void initDevParam() {
        //查询有效的参数列表:根据NDPA_CMPLEX_LEVEL对list：用来生成参数的上下级关系
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("NDPA_STATUS", SysConfigConstant.STATUS_OK);
        queryWrapper.orderByAsc("NDPA_CMPLEX_LEVEL");
        List<ParaInfo> paraInfos = paraInfoService.list(queryWrapper);
        DevParaInfoContainer.initData(paraInfos, spacePresetService.list(), sysParamService);
    }

    /**
     * 加载设备日志容器
     */
    private void initDevLog() {
        int devLogSize = Integer.parseInt(sysParamService.getParaRemark1(SysConfigConstant.DEV_LOG_VIEW_SZIE));
        //初始化各设备日志
        DevLogInfoContainer.init(devLogSize,sysParamService);
    }

    /**
     * 加载设备日志容器
     */
    private void initDevAlert() {
        int devAlertInfoSize = Integer.parseInt(sysParamService.getParaRemark1(SysConfigConstant.DEV_ALERT_INFO_SZIE));
        //初始化各设备日志
        DevAlertInfoContainer.init(devAlertInfoSize,sysParamService);
    }

    /**
     * 清空缓存
     */
    public void cleanCache(){
        BaseInfoContainer.cleanCache();
        DevParaInfoContainer.cleanCache();
        DevCtrlInterInfoContainer.cleanCache();
        DevStatusContainer.cleanCache();
        load();
        //初始化设备参数容器
        initDevParam();
        DevStatusContainer.init(BeanFactoryUtil.getBean(ISysParamService.class));
        DevCtrlInterInfoContainer.initData();
    }
}
