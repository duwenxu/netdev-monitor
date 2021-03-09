package com.xy.netdev.container;

import com.xy.netdev.common.util.ParaHandlerUtil;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.monitor.vo.DevInterParam;

import java.util.*;

/**
 * <p>
 *基础信息容器类
 * </p>
 *
 * @author sunchao
 * @since 2021-03-08
 */
public class BaseInfoContainer {
    /**
     * 设备MAP K设备IP地址 V设备信息
     */
    private static Map<String, BaseInfo> devMap = new HashMap<>();
    /**
     * 接口关联参数MAP K设备类型+接口编码 V设备参数列表信息
     */
    private static Map<String, DevInterParam> InterLinkParaMap = new HashMap<>();

    /**
     * 参数map K参数编号+命令标识 V参数信息
     */
    private static Map<String, ParaInfo> paramMap = new HashMap<>();


    /**
     * @功能：添加设备MAP
     * @param devList    设备列表
     * @return
     */
    public static void addDevMap(List<BaseInfo> devList) {
        devList.forEach(baseInfo -> {
            devMap.put(baseInfo.getDevIpAddr(),baseInfo);
        });
    }

    /**
     * @功能：添加参数MAP
     * @param paraList  参数列表
     * @return
     */
    public static void addParaMap(List<ParaInfo> paraList) {
        paraList.forEach(paraInfo -> {
            paramMap.put(ParaHandlerUtil.genLinkKey(paraInfo.getNdpaNo(),paraInfo.getNdpaCmdMark()),paraInfo);
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
                paraInfo.setParaSeq(seq);
                point = point+Integer.valueOf(paraInfo.getNdpaByteLen());
                paraInfo.setParaStartPoint(point);
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
     * @功能：根据参数编号 和 命令标识 获取参数信息
     * @param paraNo   参数编号
     * @param cmrMark  命令标识
     * @return  设备对象
     */
    public static ParaInfo getParaInfo(String paraNo,String cmrMark){
        return paramMap.get(ParaHandlerUtil.genLinkKey(paraNo,cmrMark));
    }

    /**
     * @功能：根据设备类型  和  接口编码 获取接口解析参数列表
     * @param devType     设备类型
     * @param itfCode     接口编码
     * @return  接口解析参数列表
     */
    public static List<ParaInfo> getInterLinkParaList(String devType,String itfCode){
        return InterLinkParaMap.get(ParaHandlerUtil.genLinkKey(devType,itfCode)).getDevParamList();
    }

    /**
     * @功能：根据设备类型  和  接口编码 获取接口信息
     * @param devType     设备类型
     * @param itfCode     接口编码
     * @return  接口解析参数列表
     */
    public static Interface getInterLinkInterface(String devType, String itfCode){
        return InterLinkParaMap.get(ParaHandlerUtil.genLinkKey(devType,itfCode)).getDevInterface();
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
