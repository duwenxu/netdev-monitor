package com.xy.netdev.container;


import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.monitor.bo.DevStatusInfo;
import com.xy.netdev.monitor.entity.BaseInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *设备状态信息容器类
 * </p>
 *
 * @author tangxl
 * @since 2021-03-12
 */
public class DevStatusContainer {

    private static ISysParamService sysParamServiceLocal;

    /**
     * 设备日志信息MAP K设备编号 V设备状态信息
     */
    private static Map<String,DevStatusInfo> devStatusMap = new HashMap<>();

    /**
     * @功能：当系统启动时,进行初始化各设备报警信息
     */
    public static void init(ISysParamService sysParamService){
        sysParamServiceLocal = sysParamService;
        BaseInfoContainer.getDevNos().forEach(devNo -> {
            DevStatusInfo devStatusInfo = new DevStatusInfo();
            BaseInfo devInfo = BaseInfoContainer.getDevInfoByNo(devNo);
            List<BaseInfo> masterSlaveDevList = BaseInfoContainer.getDevsFatByDevNo(devNo);
            devStatusInfo.setDevNo(devNo);
            devStatusInfo.setDevTypeCode(sysParamServiceLocal.getParaRemark1(devInfo.getDevType()));
            devStatusInfo.setDevDeployType(devInfo.getDevDeployType());
            devStatusInfo.setIsInterrupt(SysConfigConstant.RPT_DEV_STATUS_ISINTERRUPT_NO);
            devStatusInfo.setIsAlarm(SysConfigConstant.RPT_DEV_STATUS_ISALARM_NO);
            devStatusInfo.setIsUseStandby(initDevIsUseStandby(devInfo,masterSlaveDevList));
            devStatusInfo.setMasterOrSlave(initDevMasterOrSlave(devStatusInfo.getIsUseStandby(),masterSlaveDevList));
//            devStatusInfo.setWorkStatus(sysParamServiceLocal.getParaRemark1(devInfo.getDevStatus()));
            devStatusInfo.setWorkStatus("0");
            devStatusMap.put(devNo,devStatusInfo);
        });
    }


    /**
     * @功能：初始化设备是否启用主备
     * @param devInfo               设备信息
     * @param masterSlaveDevList    主备列表
     * @return 是否启用主备状态
     */
    private  static String initDevIsUseStandby(BaseInfo devInfo,List<BaseInfo> masterSlaveDevList) {
        if(!devInfo.getDevDeployType().equals(SysConfigConstant.DEV_DEPLOY_GROUP)){
            if(masterSlaveDevList.size()>1){
                return SysConfigConstant.RPT_DEV_STATUS_USESTANDBY_YES;
            }
        }
        return SysConfigConstant.RPT_DEV_STATUS_USESTANDBY_NO;
    }
    /**
     * @功能：初始化设备主用还是备用
     * @param isUseStandby          是否启用主备状态     *
     * @param masterSlaveDevList    主备列表
     * @return 主用还是备用
     */
    private  static String initDevMasterOrSlave(String isUseStandby,List<BaseInfo> masterSlaveDevList) {
        if(isUseStandby.equals(SysConfigConstant.RPT_DEV_STATUS_USESTANDBY_YES)){
            BaseInfo isUseDevInfo = null;
            for(BaseInfo devBaseInfo:masterSlaveDevList){
                if(devBaseInfo.getDevUseStatus().equals(SysConfigConstant.DEV_USE_STATUS_INUSE)){
                    isUseDevInfo = devBaseInfo;
                    break;
                }
            }
            if(isUseDevInfo!=null&&isUseDevInfo.getDevDeployType().equals(SysConfigConstant.DEV_DEPLOY_SLAVE)){
                    return SysConfigConstant.RPT_DEV_STATUS_MASTERORSLAVE_SLAVE;
            }
        }
        return SysConfigConstant.RPT_DEV_STATUS_MASTERORSLAVE_MASTER;
    }


    /**
     * @功能：添加设备中断状态
     * @param devNo    设备编号
     * @param isInterrupt    中断状态
     * @return 状态是否发生改变  true 改变  false 未改变
     */
    public synchronized static boolean setInterrupt(String devNo,String isInterrupt) {
        DevStatusInfo devStatusInfo = devStatusMap.get(devNo);
        if(!devStatusInfo.getIsInterrupt().equals(isInterrupt)){
            devStatusInfo.setIsInterrupt(isInterrupt);
            return true;
        }
        return false;
    }

    /**
     * @功能：添加设备报警状态
     * @param devNo    设备编号
     * @param isAlarm    报警状态
     * @return 状态是否发生改变  true 改变  false 未改变
     */
    public synchronized static boolean setAlarm(String devNo,String isAlarm) {
        DevStatusInfo devStatusInfo = devStatusMap.get(devNo);
        if(!devStatusInfo.getIsAlarm().equals(isAlarm)){
            devStatusInfo.setIsAlarm(isAlarm);
            return true;
        }
        return false;
    }

    /**
     * @功能：添加设备启用主备状态
     * @param devNo    设备编号
     * @param isUseStandby    启用主备状态
     * @return 状态是否发生改变  true 改变  false 未改变
     */
    public synchronized static boolean setUseStandby(String devNo,String isUseStandby) {
        DevStatusInfo devStatusInfo = devStatusMap.get(devNo);
        if(!devStatusInfo.getIsUseStandby().equals(isUseStandby)){
            devStatusInfo.setIsUseStandby(isUseStandby);
            return true;
        }
        return false;
    }


    /**
     * @功能：添加设备主用还是备用状态
     * @param devNo    设备编号
     * @param masterOrSlave    主用还是备用状态
     * @return 状态是否发生改变  true 改变  false 未改变
     */
    public synchronized static boolean setMasterOrSlave(String devNo,String masterOrSlave) {
        //设置主备列表中非当前变化设备的主备状态
        List<BaseInfo> masterSlaveDevList = BaseInfoContainer.getDevsFatByDevNo(devNo);
        masterSlaveDevList.forEach(devInfo->{
            if(!devInfo.getDevNo().equals(devNo)){
                if(masterOrSlave.equals(SysConfigConstant.RPT_DEV_STATUS_MASTERORSLAVE_SLAVE)){
                    devStatusMap.get(devInfo.getDevNo()).setMasterOrSlave(SysConfigConstant.RPT_DEV_STATUS_MASTERORSLAVE_MASTER);
                }else if(masterOrSlave.equals(SysConfigConstant.RPT_DEV_STATUS_MASTERORSLAVE_MASTER)){
                    devStatusMap.get(devInfo.getDevNo()).setMasterOrSlave(SysConfigConstant.RPT_DEV_STATUS_MASTERORSLAVE_SLAVE);
                }
            }
        });
        //设置当前设备的主备状态,如果发生变化返回true
        DevStatusInfo devStatusInfo = devStatusMap.get(devNo);
        if(!devStatusInfo.getMasterOrSlave().equals(masterOrSlave)){
            devStatusInfo.setMasterOrSlave(masterOrSlave);
            return true;
        }
        return false;
    }


    /**
     * @功能：添加设备工作状态
     * @param devNo    设备编号
     * @param workStatus    工作状态
     * @return 状态是否发生改变  true 改变  false 未改变
     */
    public synchronized static boolean setWorkStatus(String devNo,String workStatus) {
        DevStatusInfo devStatusInfo = devStatusMap.get(devNo);
        if(!devStatusInfo.getWorkStatus().equals(workStatus)){
            devStatusInfo.setWorkStatus(workStatus);
            return true;
        }
        return false;
    }


    /**
     * @功能：获取所有设备状态上报信息
     * @return  设备状态列表
     */
    public static List<DevStatusInfo> getAllDevStatusInfoList(){
        return new ArrayList(devStatusMap.values());
    }

    /**
     * @功能：获取单个设备状态上报信息
     * @param devNo    设备编号
     * @return  设备状态列表
     */
    public static DevStatusInfo getDevStatusInfo(String devNo){
        return devStatusMap.get(devNo);
    }

}
