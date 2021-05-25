package com.xy.netdev.container;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.collection.FixedSizeMap;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.DateTools;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.AlertInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.service.IAlertInfoService;
import com.xy.netdev.transit.util.DataHandlerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *设备告警信息容器类
 * </p>
 *
 * @author tangxl
 * @since 2021-03-08
 */
@Component
public class DevAlertInfoContainer {

    private static IAlertInfoService alertInfoService;

    @Autowired
    public  void setAlertInfoService(IAlertInfoService alertInfoService) {
        this.alertInfoService = alertInfoService;
    }

    /**
     * 设备日志信息MAP K设备编号 V按照时间排序的定长告警信息
     */
    private static Map<String,Map<String,FixedSizeMap<String, AlertInfo>>> devAlertInfoMap = new HashMap<>();


    private  static   int DEV_PARA_ALERT_SIZE = 10;
    /**
     * @功能：当系统启动时,进行初始化各设备报警信息
     */
    public static void init(int devAlertInfoSize){
        DEV_PARA_ALERT_SIZE = devAlertInfoSize;
        BaseInfoContainer.getDevNos().forEach(devNo -> {
            devAlertInfoMap.put(devNo,new HashMap<>());
        });
    }

    /**
     * @功能：添加设备告警信息
     * @param alertInfo    设备告警信息
     * @return
     */
    public synchronized static void addAlertInfo(AlertInfo alertInfo) {
        Map<String,FixedSizeMap<String, AlertInfo>>  devMap = devAlertInfoMap.get(alertInfo.getDevNo());
        if(devMap.containsKey(alertInfo.getNdpaNo())){
            devMap.get(alertInfo.getNdpaNo()).put(DateTools.getDateTime(),alertInfo);
        }else{
            FixedSizeMap<String, AlertInfo>  paraAlertMap = new FixedSizeMap(DEV_PARA_ALERT_SIZE);
            paraAlertMap.put(DateTools.getDateTime(),alertInfo);
            devMap.put(alertInfo.getNdpaNo(),paraAlertMap);
        }
        alertInfoService.save(alertInfo);
    }



    /**
     * @功能：根据设备编号 和 参数编号 查询最新的报警信息
     * @param devNo           设备编号
     * @param paraNo          参数编号
     * @return  设备报警信息列表
     */
    public static AlertInfo getDevParaAlertInfo(String devNo,String paraNo){
        Map<String,FixedSizeMap<String, AlertInfo>>  devMap = devAlertInfoMap.get(devNo);
        if(devMap.containsKey(paraNo)){
            return devMap.get(paraNo).getMap().lastEntry().getValue();
        }else{
            return genOkAlertInfo(devNo,paraNo);
        }
    }
    /**
     * 为了响应54所查询参数报警,该参数未报警时,生成未报警报警信息
     */
    private static AlertInfo  genOkAlertInfo(String devNo,String paraNo){
        BaseInfo devInfo = BaseInfoContainer.getDevInfoByNo(devNo);
        FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByNo(devInfo.getDevType(),paraNo);
        ISysParamService sysParamService = BaseInfoContainer.getSysParamService();
        AlertInfo  alertInfo = new AlertInfo();
        alertInfo.setDevType(devInfo.getDevType());
        alertInfo.setDevNo(devNo);
        alertInfo.setNdpaNo(paraNo);
        alertInfo.setAlertNum(1);
        alertInfo.setAlertTime(DateTools.getDateTime());
        alertInfo.setAlertStationNo(sysParamService.getParaRemark1(SysConfigConstant.PUBLIC_PARA_STATION_NO));
        alertInfo.setAlertLevel(sysParamService.getParaRemark1(SysConfigConstant.ALERT_LEVEL_OK));
        alertInfo.setAlertDesc(DataHandlerHelper.genAlertDesc(devInfo,paraInfo));
        return alertInfo;
    }
    /**
     * @功能：根据设备编号 和 基准时间 返回大于等于基准时间的 报警信息
     * @param devNo           设备编号
     * @return  设备报警信息列表
     */
    public static List<AlertInfo> getDevAlertInfoList(String devNo){
        List<AlertInfo> alertInfoList = new ArrayList<>();
        Map<String,FixedSizeMap<String, AlertInfo>>  devMap = devAlertInfoMap.get(devNo);
        if(devMap != null){
            for(String paraNo:devMap.keySet()){
                alertInfoList.add(getDevParaAlertInfo(devNo,paraNo));
            }
        }
        return alertInfoList;
    }

    /**
     * @功能：根据基准时间 返回大于等于基准时间的 所有报警信息
     * @return  设备报警信息列表
     */
    public static List<AlertInfo> getAllDevAlertInfoList(){
        List<AlertInfo> alertInfoList = new ArrayList<>();
        BaseInfoContainer.getDevNos().forEach(devNo->{
            Map<String,FixedSizeMap<String, AlertInfo>>  devMap = devAlertInfoMap.get(devNo);
            for(String paraNo:devMap.keySet()){
                alertInfoList.add(getDevParaAlertInfo(devNo,paraNo));
            }
        });
        return alertInfoList;
    }

}
