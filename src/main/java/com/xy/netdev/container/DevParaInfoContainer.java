package com.xy.netdev.container;

import com.alibaba.fastjson.JSONArray;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.monitor.bo.ParaSpinnerInfo;
import com.xy.netdev.monitor.entity.ParaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * <p>
 *设备参数信息容器类
 * </p>
 *
 * @author tangxl
 * @since 2021-03-08
 */
@Component
public class DevParaInfoContainer {
    /**
     * 系统参数服务类
     */
    private static ISysParamService sysParamService;
    /**
     * 设备参数MAP K设备  V设备参数信息
     */
    private static Map<String, Map<String,ParaInfo>> devParaMap = new HashMap<>();

    @Autowired
    public void setSysParamService(ISysParamService sysParamService){
        this.sysParamService = sysParamService;
    }


    /**
     * @功能：当系统启动时,进行初始化各设备日志
     */
    public static void init(){
        BaseInfoContainer.getDevNos().forEach(devNo -> {
            devParaMap.put(devNo,new HashMap<>());
        });
    }

    /**
     * @功能：添加设备参数MAP
     * @param paraList  参数列表
     * @return
     */
    public static void addDevParaMap(List<ParaInfo> paraList) {
        paraList.forEach(paraInfo -> {
            attachParaInfo(paraInfo);
        });
        Map<String,List<ParaInfo>> paraMapByDevType = paraList.stream().collect(Collectors.groupingBy(ParaInfo::getDevType));
        devParaMap.keySet().forEach(devNo->{
            String devType = BaseInfoContainer.getDevInfoByNo(devNo).getDevType();
            if(paraMapByDevType.containsKey(devType)){

            }
        });
    }
    /**
     * @功能：添加设备附加信息
     */
    private static void attachParaInfo(ParaInfo paraInfo){
        List<ParaSpinnerInfo>  spinnerInfoList = JSONArray.parseArray(paraInfo.getNdpaSelectData(), ParaSpinnerInfo.class);
        //paraInfo.setSpinnerInfoList(spinnerInfoList);
        //paraInfo.setDevTypeCode(sysParamService.getParaRemark1(paraInfo.getDevType()));
    }
    /**
     * @功能：添加设备MAP
     * @param devNo    设备编号
     * @return
     */
    public static void addDevMap(String devNo) {

    }


    /**
     * @功能：根据设备IP地址 获取设备信息
     * @param devNo        设备编号
     * @return  设备对象
     */
    public static String   getDevInfo(String devNo){
        return "";
    }



}
