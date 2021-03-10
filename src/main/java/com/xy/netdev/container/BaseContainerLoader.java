package com.xy.netdev.container;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.monitor.entity.*;
import com.xy.netdev.monitor.service.*;
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
    @Autowired
    private IAlertInfoService alertInfoService;

    /**
     * 初始化信息加载
     */
    @PostConstruct
    public void load(){
        //初始化基础信息
        initBaseInfo();
        //初始化设备容器
        //initDevInfo();
        //初始化设备日志容器
        initDevLog();
        //初始化告警信息
        initDevAlert();
        //初始化设备参数容器
        DevParaInfoContainer.init();
    }

    /**
     * 加载基础信息容器
     */
    private void initBaseInfo(){
        //查询有效的设备列表
        List<BaseInfo> devs = baseInfoService.list().stream().filter(baseInfo -> baseInfo.getDevStatus().equals(SysConfigConstant.DEV_STATUS_NEW)).collect(Collectors.toList());
        //查询有效的参数列表
        List<ParaInfo> paraInfos = paraInfoService.list().stream().filter(paraInfo -> paraInfo.getNdpaStatus().equals(SysConfigConstant.STATUS_OK)).collect(Collectors.toList());
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
