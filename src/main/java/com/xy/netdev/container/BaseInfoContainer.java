package com.xy.netdev.container;

import com.xy.netdev.common.util.ParaHandlerUtil;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.monitor.bo.DevInterParam;
import com.xy.netdev.monitor.entity.PrtclFormat;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

/**
 * <p>
 *基础信息容器类
 * </p>
 *
 * @author sunchao
 * @since 2021-03-08
 */
@Slf4j
public class BaseInfoContainer {

    /**
     * 设备MAP K设备IP地址 V设备信息
     */
    private static Map<String, BaseInfo> devMap = new HashMap<>();

    /**
     * 设备MAP K设备编号 V设备信息
     */
    private static Map<String, BaseInfo> devNoMap = new HashMap<>();

    /**
     * 接口关联参数MAP K设备类型+接口编码 V设备参数列表信息
     */
    private static Map<String, DevInterParam> InterLinkParaMap = new HashMap<>();

    /**
     * 参数map K设备类型+命令标识 V参数信息
     */
    private static Map<String, ParaInfo> paramCmdMap = new HashMap<>();

    /**
     * 参数map K设备类型+参数编号 V参数信息
     */
    private static Map<String, ParaInfo> paramNoMap = new HashMap<>();

    /**
     * @功能：添加设备MAP
     * @param devList    设备列表
     * @return
     */
    public static void addDevMap(List<BaseInfo> devList) {
        devList.forEach(baseInfo -> {
            try {
                devMap.put(baseInfo.getDevIpAddr(),baseInfo);
                devNoMap.put(baseInfo.getDevNo(),baseInfo);
            } catch (Exception e) {
                log.error("设备["+baseInfo.getDevNo()+"]ip地址或设备编号存在异常，请检查:"+e.getMessage());
            }
        });
    }

    /**
     * @功能：添加参数MAP
     * @param paraList  参数列表
     * @return
     */
    public static void addParaMap(List<ParaInfo> paraList) {
        paraList.forEach(paraInfo -> {
            try {
                paramCmdMap.put(ParaHandlerUtil.genLinkKey(paraInfo.getDevType(),paraInfo.getNdpaCmdMark()),paraInfo);
                paramNoMap.put(ParaHandlerUtil.genLinkKey(paraInfo.getDevType(),paraInfo.getNdpaNo()),paraInfo);
            } catch (Exception e) {
                log.error("参数["+paraInfo.getNdpaCode()+"]的设备类型或命令标识或参数编号存在异常，请检查:"+e.getMessage());
            }
        });
    }

    /**
     * @功能：添加接口关联参数MAP
     * @param devInterParamList    设备接口参数list
     * @return
     */
    public static void addInterLinkParaMap(List<DevInterParam> devInterParamList) {
        //修改各参数序号和下标
        devInterParamList.forEach(devInterParam -> {
            int seq = 1;
            Integer point = 0 ;
            for (ParaInfo paraInfo : devInterParam.getDevParamList()) {
                try {
                    paraInfo.setParaSeq(seq);
                    point = point+Integer.valueOf(paraInfo.getNdpaByteLen());
                    paraInfo.setParaStartPoint(point);
                } catch (NumberFormatException e) {
                    log.error("参数["+paraInfo.getNdpaCode()+"]的字节长度存在异常，请检查："+e.getMessage());
                }
            }
            InterLinkParaMap.put(devInterParam.getId(),devInterParam);
        });
    }

    /**
     * @功能：根据设备IP地址 获取设备信息
     * @param devIPAddr    设备IP地址
     * @return  设备对象
     */
    public static BaseInfo getDevInfo(String devIPAddr){
        return devMap.get(devIPAddr);
    }

    /**
     * @功能：根据设备编号 获取设备信息
     * @param devNo    设备编号
     * @return  设备对象
     */
    public static BaseInfo getDevInfoByNo(String devNo){
        return devNoMap.get(devNo);
    }

    /**
     * @功能：根据设备类型 和 命令标识 获取参数信息
     * @param devType  设备类型
     * @param cmrMark  命令标识
     * @return  设备对象
     */
    public static ParaInfo getParaInfoByCmd(String devType,String cmrMark){
        return paramCmdMap.get(ParaHandlerUtil.genLinkKey(devType,cmrMark));
    }
    /**
     * @功能：根据 设备类型 和 参数编号 获取参数信息
     * @param devType  设备类型
     * @param ndpaNo   命令标识
     * @return  设备对象
     */
    public static ParaInfo getParaInfoByNo(String devType,String ndpaNo){
        return paramNoMap.get(ParaHandlerUtil.genLinkKey(devType,ndpaNo));
    }

    /**
     * @功能：根据设备类型  和  接口编码 获取接口解析参数列表
     * @param devType     设备类型
     * @param itfCode     接口编码
     * @return  接口解析参数列表
     */
    public static List<ParaInfo> getInterLinkParaList(String devType,String itfCode){
        DevInterParam devInterParam = InterLinkParaMap.get(ParaHandlerUtil.genLinkKey(devType,itfCode));
        if(devInterParam == null){
            return devInterParam.getDevParamList();
        }
        return null;
    }

    /**
     * @功能：根据设备类型  和  接口编码 获取接口信息
     * @param devType     设备类型
     * @param itfCode     接口编码
     * @return  接口解析参数列表
     */
    public static Interface getInterLinkInterface(String devType, String itfCode){
        DevInterParam devInterParam = InterLinkParaMap.get(ParaHandlerUtil.genLinkKey(devType,itfCode));
        if(devInterParam == null){
            return devInterParam.getDevInterface();
        }
        return null;
    }

    /**
     * @功能：根据设备类型  和  接口编码 获取接口信息
     * @param devType     设备类型
     * @param itfCode     接口编码
     * @return  接口解析参数列表
     */
    public static PrtclFormat getInterLinkFmtFormat(String devType, String itfCode){
        DevInterParam devInterParam = InterLinkParaMap.get(ParaHandlerUtil.genLinkKey(devType,itfCode));
        if(devInterParam == null){
            return devInterParam.getInterfacePrtcl();
        }
        return null;
    }

    /**
     * @功能：获取所有设备信息集合
     * @return  设备对象
     */
    public static Collection<BaseInfo> getDevInfos(){
        return devMap.values();
    }

    /**
     * @功能：获取所有设备编号集合
     * @return  设备对象
     */
    public static Set<String> getDevNos(){
        return devMap.keySet();
    }

}
