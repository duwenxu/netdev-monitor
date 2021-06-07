package com.xy.netdev.container;


import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.monitor.bo.DevStatusInfo;
import com.xy.netdev.monitor.bo.ParaViewInfo;
import com.xy.netdev.monitor.entity.BaseInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
     * 设备上报参数状态记录信息
     */
    private static Map<String,Map<String,Map<String,String>>> devParamRptMap  = new HashMap<>();

    private static String EXCEPTION_STATUS = "1";

    /**
     * @功能：当系统启动时,进行初始化各设备报警信息
     */
    public static void init(ISysParamService sysParamService){
        sysParamServiceLocal = sysParamService;
        BaseInfoContainer.getDevInfos().forEach(devInfo -> {
            DevStatusInfo devStatusInfo = new DevStatusInfo();
            List<BaseInfo> masterSlaveDevList = BaseInfoContainer.getDevsFatByDevNo(devInfo.getDevNo());
            devStatusInfo.setDevNo(devInfo.getDevNo());
            devStatusInfo.setDevTypeCode(sysParamServiceLocal.getParaRemark1(devInfo.getDevType()));
            devStatusInfo.setIsInterrupt(SysConfigConstant.RPT_DEV_STATUS_ISINTERRUPT_NO);
            devStatusInfo.setIsAlarm(SysConfigConstant.RPT_DEV_STATUS_ISALARM_NO);
            devStatusInfo.setIsUseStandby(initDevIsUseStandby(devInfo,masterSlaveDevList));
//            devStatusInfo.setWorkStatus(sysParamServiceLocal.getParaRemark1(devInfo.getDevStatus()));
            devStatusInfo.setWorkStatus("0");
            devStatusInfo.setMasterOrSlave(initDevMasterOrSlave(devStatusInfo.getIsUseStandby(),masterSlaveDevList));
            devStatusInfo.setDevDeployType(devInfo.getDevDeployType());
            //君威功放特殊处理  sunchao
            if(SysConfigConstant.DEVICE_CAR_GF.equals(devInfo.getDevType())){
                //通过主BUC射频开关来判断主备
                ParaViewInfo paraInfo = DevParaInfoContainer.getDevParaView(devInfo.getDevNo(),"15");
                if("1".equals(paraInfo.getParaVal())){
                    //主
                    devStatusInfo.setDevDeployType(SysConfigConstant.DEV_DEPLOY_MASTER);
                    devStatusInfo.setMasterOrSlave(SysConfigConstant.RPT_DEV_STATUS_MASTERORSLAVE_MASTER);
                }else{
                    //备
                    devStatusInfo.setDevDeployType(SysConfigConstant.DEV_DEPLOY_SLAVE);
                    devStatusInfo.setMasterOrSlave(SysConfigConstant.RPT_DEV_STATUS_MASTERORSLAVE_SLAVE);
                }
            }
            devStatusMap.put(devInfo.getDevNo(),devStatusInfo);
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
    public synchronized static boolean setInterrupt(String devNo,String isInterrupt,String rptType) {
        isInterrupt = getDevAllPramsStatus(devNo,isInterrupt,rptType);
        DevStatusInfo devStatusInfo = devStatusMap.get(devNo);
        if(!devStatusInfo.getIsInterrupt().equals(isInterrupt)){
            devStatusInfo.setIsInterrupt(isInterrupt);
            devStatusMap.put(devNo,devStatusInfo);
            return true;
        }
        return false;
    }



    /**
     * @功能：添加设备报警状态
     * @param devNo    设备编号
     * @param isAlarm    报警状态
     * @param rptType    上报类型
     * @return 状态是否发生改变  true 改变  false 未改变
     */
    public synchronized static boolean setAlarm(String devNo,String isAlarm,String rptType) {
        isAlarm = getDevAllPramsStatus(devNo,isAlarm,rptType);
        DevStatusInfo devStatusInfo = devStatusMap.get(devNo);
        if(!devStatusInfo.getIsAlarm().equals(isAlarm)){
            devStatusInfo.setIsAlarm(isAlarm);
            return true;
        }
        if(isAlarm.equals("1")){
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
    public synchronized static boolean setUseStandby(String devNo,String isUseStandby,String rptType) {
        isUseStandby = getDevAllPramsStatus(devNo,isUseStandby,rptType);
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
    public synchronized static boolean setMasterOrSlave(String devNo,String masterOrSlave,String rptType) {
        masterOrSlave = getDevAllPramsStatus(devNo,masterOrSlave,rptType);
        //设置主备列表中非当前变化设备的主备状态
        BaseInfo baseInfo = BaseInfoContainer.getDevInfoByNo(devNo);
        String devType = baseInfo.getDevType();
        List<BaseInfo> masterSlaveDevList = BaseInfoContainer.getDevsFatByDevNo(devNo);
        if(devType.equals(SysConfigConstant.DEVICE_QHDY)){
            return handleMasterOfBPQ(masterSlaveDevList,masterOrSlave);
        }else if (baseInfo.getDevType().equals(SysConfigConstant.DEVICE_TRANS_SWITCH)){
            return handlerMasterOfModem(devNo,masterOrSlave);
        }else if (baseInfo.getDevType().equals(SysConfigConstant.DEVICE_CAR_GF)){
            return handlerMasterOfGf(devNo,masterOrSlave);
        } else {
            for (BaseInfo devInfo : masterSlaveDevList) {
                if(!devInfo.getDevNo().equals(devNo)){
                    if(masterOrSlave.equals(SysConfigConstant.RPT_DEV_STATUS_MASTERORSLAVE_SLAVE)){
                        devStatusMap.get(devInfo.getDevNo()).setMasterOrSlave(SysConfigConstant.RPT_DEV_STATUS_MASTERORSLAVE_MASTER);
                    }else if(masterOrSlave.equals(SysConfigConstant.RPT_DEV_STATUS_MASTERORSLAVE_MASTER)){
                        devStatusMap.get(devInfo.getDevNo()).setMasterOrSlave(SysConfigConstant.RPT_DEV_STATUS_MASTERORSLAVE_SLAVE);
                    }
                }
            }
            //设置当前设备的主备状态,如果发生变化返回true
            DevStatusInfo devStatusInfo = devStatusMap.get(devNo);
            if(!devStatusInfo.getMasterOrSlave().equals(masterOrSlave)){
                devStatusInfo.setMasterOrSlave(masterOrSlave);
                return true;
            }
            return false;
        }
    }

    /**
     * 处理功放的主备状态
     * @param devNo
     * @param masterOrSlave
     * @return
     */
    private static boolean handlerMasterOfGf(String devNo, String masterOrSlave) {
        AtomicBoolean changed = new AtomicBoolean(false);
        String oldStatus = DevStatusContainer.getDevStatusInfo(devNo).getMasterOrSlave();
        if (!oldStatus.equals(masterOrSlave)){
            synchronized (masterOrSlave) {
                DevStatusContainer.setModemUse(devNo, masterOrSlave);
            }
            changed.set(true);
        }
        return changed.get();
    }


    private static final Map<String,List<String>> modemPair = new ConcurrentHashMap<>();
    static {
        //初始化转换开关和调制解调器的对应关系
        modemPair.put("30", Arrays.asList("11","12"));
        modemPair.put("31",Arrays.asList("13","14"));
    }

    /**
     * 处理调制解调器的主备状态
     * @param devNo
     * @param masterOrSlave
     * @return
     */
    private static boolean handlerMasterOfModem(String devNo, String masterOrSlave) {
        //是否需要推送数据
        AtomicBoolean changed = new AtomicBoolean(false);
        List<BaseInfo> modemBaseInfo = modemPair.get(devNo)
                .stream().map(BaseInfoContainer::getDevInfoByNo).collect(Collectors.toList());
        //历史主备状态
        //主即第一个调制解调器在用，备即第二个在用
        String oldStatus = DevStatusContainer.getDevStatusInfo(modemBaseInfo.get(0).getDevNo()).getMasterOrSlave();
        if (masterOrSlave.equals("0")) {
            if (oldStatus.equals("1")) {
                synchronized (masterOrSlave) {
                    DevStatusContainer.setModemUse(modemBaseInfo.get(0).getDevNo(), "0");
                    DevStatusContainer.setModemUse(modemBaseInfo.get(1).getDevNo(), "1");
                }
                changed.set(true);
            }
        } else {
            if (oldStatus.equals("0")) {
                synchronized (masterOrSlave) {
                    DevStatusContainer.setModemUse(modemBaseInfo.get(0).getDevNo(), "1");
                    DevStatusContainer.setModemUse(modemBaseInfo.get(1).getDevNo(), "0");
                }
                changed.set(true);
            }
        }
        return changed.get();
    }

    /**
     * 处理变频器的主备状态
     * @param baseInfos
     * @param masterOrSlave
     * @return
     */
    private static boolean handleMasterOfBPQ(List<BaseInfo> baseInfos,String masterOrSlave) {
        boolean flag = false;
        for (BaseInfo baseInfo : baseInfos) {
            if(baseInfo.getDevDeployType().equals(SysConfigConstant.DEV_DEPLOY_MASTER)){
                DevStatusInfo devStatusInfo = devStatusMap.get(baseInfo.getDevNo());
                if(!devStatusInfo.getMasterOrSlave().equals(masterOrSlave)){
                    devStatusInfo.setMasterOrSlave(masterOrSlave);
                    flag = true;
                }
            }else{
                DevStatusInfo devStatusInfo = devStatusMap.get(baseInfo.getDevNo());
                String statusStr = "1";
                if(masterOrSlave.equals("1")){
                    statusStr = "0";
                }
                devStatusInfo.setMasterOrSlave(statusStr);
            }
        }
        return flag;
    }

    /**
     * @功能：添加设备主用还是备用状态(支持功放的特殊)
     * @param devNo    设备编号
     * @return 状态是否发生改变  true 改变  false 未改变
     */
    public static void setMasterOrSlave(String devNo) {
        DevStatusInfo devStatusInfo = devStatusMap.get(devNo);
        if(!SysConfigConstant.RPT_DEV_STATUS_MASTERORSLAVE_MASTER.equals(devStatusInfo.getMasterOrSlave())){
            devStatusInfo.setDevDeployType(SysConfigConstant.DEV_DEPLOY_SLAVE);
            devStatusInfo.setMasterOrSlave(SysConfigConstant.RPT_DEV_STATUS_MASTERORSLAVE_SLAVE);
        }else{
            devStatusInfo.setDevDeployType(SysConfigConstant.DEV_DEPLOY_MASTER);
            devStatusInfo.setMasterOrSlave(SysConfigConstant.RPT_DEV_STATUS_MASTERORSLAVE_MASTER);
        }
    }

    /**
     * @功能：添加设备工作状态
     * @param devNo    设备编号
     * @param workStatus    工作状态
     * @return 状态是否发生改变  true 改变  false 未改变
     */
    public synchronized static boolean setWorkStatus(String devNo,String workStatus,String rptType) {
        workStatus = getDevAllPramsStatus(devNo,workStatus,rptType);
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
        Collection collection = devStatusMap.values();
        return new ArrayList<>(collection);
    }

    /**
     * @功能：获取单个设备状态上报信息
     * @param devNo    设备编号
     * @return  设备状态列表
     */
    public static DevStatusInfo getDevStatusInfo(String devNo){
        return devStatusMap.get(devNo);
    }

    /**
     * 设置调制解调器在用状态
     * @param devNo
     * @param masterOrSlave
     */
    public static void setModemUse(String devNo, String masterOrSlave) {
        devStatusMap.get(devNo).setMasterOrSlave(masterOrSlave);
    }

    /**
     * 清空缓存
     */
    public static void cleanCache(){
        devStatusMap.clear();
    }


    /**
     * 添加设备参数状态上报记录
     * @param devNo
     * @param paramNo
     * @param rptType
     * @param status
     */
    public static void addParamStatus(String devNo,String paramNo,String rptType, String status){
        if(devParamRptMap.containsKey(devNo)){
            Map<String,Map<String,String>> paraRptMap = devParamRptMap.get(devNo);
            if(paraRptMap.containsKey(rptType)){
                paraRptMap.get(rptType).put(paramNo,status);
            }else{
                Map<String,String> paras = new HashMap<>();
                paras.put(paramNo,status);
                paraRptMap.put(rptType,paras);
            }
        }else{
            Map<String,Map<String,String>> paraRptMap = new HashMap<>();
            Map<String,String> paras = new HashMap<>();
            paras.put(paramNo,status);
            devParamRptMap.put(devNo,paraRptMap);
        }
    }

    /**
     * 获取设备参数状态上报记录
     * @return
     */
    public static Map<String,Map<String,Map<String,String>>> getDevParamRptMap(){
        return  devParamRptMap;
    }

    /**
     * 获取设备所以参数的上报状态
     * @param devNo
     * @param status
     * @param rptType
     * @return
     */
    public static String getDevAllPramsStatus(String devNo,String status,String rptType){
        Map<String,String> paraStatusMap =  devParamRptMap.get(devNo).get(rptType);
        for (String value : paraStatusMap.values()) {
            if(value.equals(EXCEPTION_STATUS)){
                status = EXCEPTION_STATUS;
                break;
            }
        }
        return status;
    }

}
