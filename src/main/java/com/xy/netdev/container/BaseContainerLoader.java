package com.xy.netdev.container;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.ParaHandlerUtil;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.monitor.service.IBaseInfoService;
import com.xy.netdev.monitor.service.IInterfaceService;
import com.xy.netdev.monitor.service.IParaInfoService;
import com.xy.netdev.monitor.service.IPrtclFormatService;
import com.xy.netdev.monitor.bo.DevInterParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
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
        //加载设备信息
        List<BaseInfo> devs = baseInfoService.list().stream().filter(baseInfo -> baseInfo.getDevStatus().equals(SysConfigConstant.DEV_STATUS_NEW)).collect(Collectors.toList());
        BaseInfoContainer.addDevMap(devs);
        List<ParaInfo> paraInfos = paraInfoService.list().stream().filter(paraInfo -> paraInfo.getNdpaStatus().equals(SysConfigConstant.STATUS_OK)).collect(Collectors.toList());
        //加载设备参数信息
        BaseInfoContainer.addParaMap(paraInfos);
        List<Interface> interfaces = interfaceService.list().stream().filter(anInterface -> anInterface.getItfStatus().equals(SysConfigConstant.STATUS_OK)).collect(Collectors.toList());
        //加载设备接口参数信息
        List<PrtclFormat> prtclList = prtclFormatService.list();
        List<DevInterParam> devInterParams = new ArrayList<>();
        //封装设备接口参数实体类list
        interfaces.forEach(anInterface -> {
            List<String> paraCodes = Arrays.asList(anInterface.getItfDataFormat().split(","));
            DevInterParam devInterParam = new DevInterParam();
            devInterParam.setId(ParaHandlerUtil.genLinkKey(anInterface.getDevType(),anInterface.getItfCode()));
            devInterParam.setInterfacePrtcl(prtclList.stream().filter(prtclFormat -> prtclFormat.getFmtId() == anInterface.getFmtId()).collect(Collectors.toList()).get(0));
            devInterParam.setDevInterface(anInterface);
            devInterParam.setDevParamList(paraInfos.stream().filter(paraInfo -> paraCodes.contains(paraInfo.getNdpaCode())).collect(Collectors.toList()));
            devInterParams.add(devInterParam);
        });
        BaseInfoContainer.addInterLinkParaMap(devInterParams);
    }



    /**
     * 加载设备日志容器
     */
    private void initDevLog(){
        int devLogSize = Integer.parseInt(sysParamService.getParaRemark1(SysConfigConstant.DEV_LOG_VIEW_SZIE));
        //初始化各设备日志
        DevLogInfoContainer.init(devLogSize,BaseInfoContainer.getDevInfos());
    }
}
