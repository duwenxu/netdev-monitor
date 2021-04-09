package com.xy.netdev.container;

import cn.hutool.core.util.ObjectUtil;
import com.xy.netdev.admin.entity.SysParam;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.collection.FixedSizeMap;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.DateTools;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.entity.OperLog;
import com.xy.netdev.monitor.entity.PrtclFormat;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * <p>
 *设备日志信息容器类
 * </p>
 *
 * @author tangxl
 * @since 2021-03-08
 */
public class DevLogInfoContainer {
    /**
     * 设备参数设置响应--成功
     */
    public static final String PARA_REPS_STATUS_SUCCEED = "0";
    /**
     * 设备参数设置响应--失败
     */
    public static final String PARA_REPS_STATUS_FAIL = "1";
    /**
     * 设备参数设置响应--尚未响应
     */
    public static final String PARA_REPS_STATUS_NORESP = "2";
    /**
     * 设备参数设置响应--不合法设置
     */
    public static final String PARA_REPS_STATUS_ILLEGALSET = "3";
    /**
     * 设备日志信息MAP K设备编号 V按照时间排序的定长日志
     */
    private static Map<String, FixedSizeMap<String,OperLog>> devLogInfoMap = new HashMap<>();

    /**
     * 54所设备参数设置响应MAP K设备编号 V<K:参数编号,V:响应状态 [0:成功,1:失败]>
     */
    private static Map<String, Map<String,String>> devParaSetRespStatusMap = new HashMap<>();

    /**
     * @功能：当系统启动时,进行初始化各设备日志
     */
    public static void init(int devLogSize){
        BaseInfoContainer.getDevInfos().forEach(baseInfo -> {
            devLogInfoMap.put(baseInfo.getDevNo(),new FixedSizeMap<>(devLogSize));
            devParaSetRespStatusMap.put(baseInfo.getDevNo(),new Hashtable<>());
        });
    }
    /**
     * @功能：添加设备日志信息
     * @param devLog    设备日志信息
     * @return
     */
    public synchronized static void addDevLog(OperLog devLog) {
        String logTime = DateTools.getDateTime();
        devLog.setLogTime(logTime);
        devLogInfoMap.get(devLog.getDevNo()).put(logTime,devLog);
    }

    /**
     * @功能：添加设备参数设置响应状态
     * @param devNo             设备编号
     * @param paraNo            设备参数编号
     * @param paraRespstatus    设备参数响应状态
     * @return
     */
    public synchronized static void addParaRespStatus(String devNo,String paraNo,String paraRespstatus) {
        devParaSetRespStatusMap.get(devNo).put(paraNo,paraRespstatus);
    }

    /**
     * @功能：设备参数发送前初始化响应状态
     * @param devNo             设备编号
     * @param paraNo            设备参数编号
     * @return
     */
    public synchronized static void initParaRespStatus(String devNo,String paraNo) {
        devParaSetRespStatusMap.get(devNo).put(paraNo,PARA_REPS_STATUS_NORESP);
        //todo test
        devParaSetRespStatusMap.get("19").put("1", "0");
        devParaSetRespStatusMap.get("19").put("2", "0");
    }
    /**
     * @功能：设备参数设置前效验为只读,设置为不合法设置状态
     * @param devNo             设备编号
     * @param paraNo            设备参数编号
     * @return
     */
    public synchronized static void setParaRespIllegalStatus(String devNo,String paraNo) {
        devParaSetRespStatusMap.get(devNo).put(paraNo,PARA_REPS_STATUS_ILLEGALSET);
    }
    /**
     * @功能：根据设备编号 获取按照时间倒序排列的设备日志列表
     * @param devNo        设备编号
     * @return  设备日志列表
     */
    public static List<OperLog> getDevLogList(String devNo){
        return new ArrayList(devLogInfoMap.get(devNo).getMap().descendingMap().values());
    }
    /**
     * @功能：根据设备编号 获取该设备下所有参数响应状态
     * @param devNo        设备编号
     * @return  设备参数状态MAP
     */
    public static Map<String,String> getDevParaRespStatusList(String devNo){
        return devParaSetRespStatusMap.get(devNo);
    }
    /**
     * @功能：根据设备编号 获取该设备下指定参数响应状态
     * @param devNo        设备编号
     * @param paraNo            设备参数编号
     * @return  设备参数状态
     */
    public static String getDevParaRespStatus(String devNo,String paraNo){
        return devParaSetRespStatusMap.get(devNo).get(paraNo);
    }
    /**
     * @功能：设置设备响应日志信息
     * @param respData        协议解析响应数据
     * @return
     */
    public static void   handlerRespDevPara(FrameRespData respData) {
        ISysParamService sysParamService =BaseInfoContainer.getSysParamService();
        OperLog devLog =new OperLog();`
        devLog.setDevType(respData.getDevType());
        devLog.setDevNo(respData.getDevNo());
        devLog.setLogAccessType(respData.getAccessType());
        devLog.setLogOperType(respData.getOperType());
        devLog.setLogOperTypeName(sysParamService.getParaName(respData.getOperType()));
        devLog.setLogAccessTypeName(sysParamService.getParaName(respData.getAccessType()));
        setLogOperObj(respData.getCmdMark(),devLog);
        devLog.setLogOperContent(genRespCodeInfo(respData)+genFrameParaContent(respData.getFrameParaList()));
        devLog.setOrignData(respData.getReciveOriginalData());
        addDevLog(devLog);
    }

    /**
     * @功能：设置设备响应日志信息
     * @param reqData        协议解析请求数据
     * @return
     */
    public static void   handlerReqDevPara(FrameReqData reqData) {
        ISysParamService sysParamService =BaseInfoContainer.getSysParamService();
        OperLog devLog =new OperLog();
        devLog.setDevType(reqData.getDevType());
        devLog.setDevNo(reqData.getDevNo());
        devLog.setLogAccessType(reqData.getAccessType());
        devLog.setLogOperType(reqData.getOperType());
        devLog.setLogOperTypeName(sysParamService.getParaName(reqData.getOperType()));
        devLog.setLogAccessTypeName(sysParamService.getParaName(reqData.getAccessType()));
        setLogOperObj(reqData.getCmdMark(),devLog);
        devLog.setLogOperContent(genIsOkInfo(reqData)+genFrameParaContent(reqData.getFrameParaList()));
        devLog.setOrignData(reqData.getSendOriginalData());
        addDevLog(devLog);
    }
    /**
     * @功能：设置设备响应日志信息中操作对象
     * @param cmdMark              命令标识符
     * @param devLog               日志对象
     * @return
     */
    private static void setLogOperObj(String cmdMark,OperLog devLog){
        devLog.setLogCmdMark(cmdMark);
        if(SysConfigConstant.ACCESS_TYPE_PARAM.equals(devLog.getLogAccessType())){
            FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(devLog.getDevType(),cmdMark);
            devLog.setLogOperObjName(frameParaInfo.getParaName());
            devLog.setLogOperObj(frameParaInfo.getParaId());
        }
        if(SysConfigConstant.ACCESS_TYPE_INTERF.equals(devLog.getLogAccessType())){
            Interface devInterface = BaseInfoContainer.getInterLinkInterface(devLog.getDevType(),cmdMark);
            devLog.setLogOperObjName(devInterface.getItfName());
            devLog.setLogOperObj(devInterface.getItfId());
        }
    }

    /**
     * @功能：生成传送参数内容
     * @param frameParaList        传送参数列表
     * @return
     */
    private static String genFrameParaContent(List<FrameParaData> frameParaList){
        if(frameParaList!=null&&!frameParaList.isEmpty()){
            StringBuilder  logContent = new StringBuilder();
            logContent.append(" 传送参数为:");
            frameParaList.forEach(frameParaData -> {
                String paraName = BaseInfoContainer.getParaInfoByNo(frameParaData.getDevType(),frameParaData.getParaNo()).getParaName();
                String paraVal = StringUtils.isNotBlank(frameParaData.getParaVal())?"["+frameParaData.getParaVal()+"]|": "";
                // logContent.append(paraName+"["+ frameParaData.getParaVal()+"]|");
                logContent.append(paraName+paraVal);
            });
            return logContent.toString();
        }
        return "";
    }
    /**
     * @功能：生成响应码信息
     * @param respData        协议解析响应数据
     * @return 响应码加工信息
     */
    private static String genRespCodeInfo(FrameRespData respData){
        PrtclFormat prtclFormat=BaseInfoContainer.getPrtclByInterfaceOrPara(respData.getDevType(),respData.getCmdMark());
        ISysParamService sysParamService =BaseInfoContainer.getSysParamService();
        String respCodeParaCd = "";//响应码对应的参数编码
        if(respData.getOperType().equals(SysConfigConstant.OPREATE_QUERY_RESP)){
            respCodeParaCd = prtclFormat.getFmtScType();
        }
        if(respData.getOperType().equals(SysConfigConstant.OPREATE_CONTROL_RESP)){
            respCodeParaCd = prtclFormat.getFmtCcType();
        }
        SysParam respCodeParam = new SysParam();
        if(!StringUtils.isEmpty(respCodeParaCd)){
            String parentCode = sysParamService.getParaRemark1(respCodeParaCd);
            if(!StringUtils.isEmpty(parentCode)){
                List<SysParam> paramList = sysParamService.queryParamsByParentId(parentCode);
                for(SysParam sysParam:paramList){
                    if(sysParam.getRemark1().equals(respData.getRespCode())){
                        respCodeParam = sysParam;
                        break;
                    }
                }
            }
        }
        setReqParaRepsStatus(respCodeParam,respData);
        if(StringUtils.isEmpty(respCodeParam.getParaName())){
            return "";
        }
        return  "执行结果:"+respCodeParam.getParaName();
    }

    /**
     * @功能：生成发送是否成功
     * @param reqData        协议解析请求数据
     * @return 发送是否成功信息
     */
    private static String genIsOkInfo(FrameReqData reqData){
        if (ObjectUtil.isNull(reqData.getIsOk())){
         return "";
        }
        if(reqData.getIsOk().equals("0")){
            return "执行结果:发送成功";
        }
        setReqParaRepsStatus("1",reqData);
        return "执行结果:发送失败";
    }
    /**
     * @功能：设置参数控制请求响应状态
     * @param repsStatus        响应状态
     * @param reqData        协议解析请求数据
     * @return
     */
    private static void setReqParaRepsStatus(String repsStatus,FrameReqData reqData){
        //当参数控制发送时,未发送成功时,设置参数响应状态为失败
        if(reqData.getOperType().equals(SysConfigConstant.OPREATE_CONTROL)&&!repsStatus.equals("0")){
            if(reqData.getFrameParaList()!=null&&!reqData.getFrameParaList().isEmpty()){
                reqData.getFrameParaList().forEach(frameParaData -> {
                    addParaRespStatus(reqData.getDevNo(),frameParaData.getParaNo(),PARA_REPS_STATUS_FAIL);
                });
            }
        }
    }
    /**
     * @功能：设置参数控制响应中响应状态
     * @param respCodeParam   响应码参数对象
     * @param respData        协议解析响应数据
     * @return
     */
    private static void setReqParaRepsStatus(SysParam respCodeParam,FrameRespData respData){
        if(respData.getOperType().equals(SysConfigConstant.OPREATE_CONTROL_RESP)){
            String paraRepsStatus = PARA_REPS_STATUS_SUCCEED;
            if(!StringUtils.isEmpty(respCodeParam.getRemark2())){//当remark2配置了,按照remark2设置
                paraRepsStatus = respCodeParam.getRemark2().trim();
            }else if(!StringUtils.isEmpty(respData.getRespCode())){//remark2未配置,响应码有值
                if(!respData.getRespCode().trim().equals(PARA_REPS_STATUS_SUCCEED)){
                    paraRepsStatus = PARA_REPS_STATUS_FAIL;
                }
            }
            if(respData.getFrameParaList()!=null&&!respData.getFrameParaList().isEmpty()){
                for(FrameParaData frameParaData:respData.getFrameParaList()){
                    addParaRespStatus(respData.getDevNo(),frameParaData.getParaNo(),paraRepsStatus);
                };
            }
        }
    }



}
