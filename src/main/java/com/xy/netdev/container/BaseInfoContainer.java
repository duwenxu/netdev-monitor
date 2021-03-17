package com.xy.netdev.container;

import com.alibaba.fastjson.JSONArray;
import com.xy.common.exception.BaseException;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.ParaHandlerUtil;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.bo.ParaSpinnerInfo;
import com.xy.netdev.monitor.entity.*;
import com.xy.netdev.monitor.bo.DevInterParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *基础信息容器类
 * </p>
 *
 * @author sunchao
 * @since 2021-03-08
 */
@Slf4j
@Component
public class BaseInfoContainer {

    /**
     * 系统参数服务类
     */
    private static ISysParamService sysParamService;
    @Autowired
    public void setSysParamService(ISysParamService sysParamService){
        this.sysParamService = sysParamService;
    }

    public static ISysParamService getSysParamService(){
        return sysParamService;
    }
    /**
     * 设备MAP K设备IP地址 V设备信息
     */
    private static Map<String, BaseInfo> devMap = new HashMap<>();

    /**
     * 设备MAP K设备编号 V设备信息
     */
    private static Map<String, BaseInfo> devNoMap = new HashMap<>();

    /**
     * 接口关联参数MAP K设备类型+命令标识 V设备参数列表信息
     */
    private static Map<String, DevInterParam> InterLinkParaMap = new HashMap<>();

    /**
     * 参数map K设备类型+命令标识 V参数信息
     */
    private static Map<String, FrameParaInfo> paramCmdMap = new HashMap<>();

    /**
     * 参数map K设备类型+参数编号 V参数信息
     */
    private static Map<String, FrameParaInfo> paramNoMap = new HashMap<>();

    /**
     * 参数map K设备类型 V接口list
     */
    private static Map<String, List<Interface>> devTypeInterMap = new HashMap<>();

    /**
     * 参数map K设备类型 V参数list
     */
    private static Map<String, List<FrameParaInfo>> devTypeParamMap = new HashMap<>();

    /**
     * @功能：当系统启动时,进行初始化各设备日志
     */
    public static void init(List<BaseInfo> devs,List<ParaInfo> paraInfos,List<Interface> interfaces,List<PrtclFormat> prtclList){
        //将设备参数转化为帧参数
        List<FrameParaInfo> frameParaInfos = changeDevParaToFrame(paraInfos,prtclList);
        //加载设备信息
        addDevMap(devs);
        //加载设备类型对应的接口list
        addDevTypeInterMap(interfaces);
        //加载设备类型对应的参数list
        addDevTypeParaMap(frameParaInfos);
        //加载设备参数信息
        addParaMap(frameParaInfos);
        //加载设备接口参数信息
        List<DevInterParam> devInterParams = new ArrayList<>();
        //封装设备接口参数实体类list
        interfaces.forEach(anInterface -> {
            List<String> paraIds = StringUtils.isBlank(anInterface.getItfDataFormat())? new ArrayList<>():Arrays.asList(anInterface.getItfDataFormat().split(","));
            DevInterParam devInterParam = new DevInterParam();
            devInterParam.setId(ParaHandlerUtil.genLinkKey(anInterface.getDevType(),anInterface.getItfCmdMark()));
            List<PrtclFormat> prtclFormats = prtclList.stream().filter(prtclFormat -> prtclFormat.getFmtId() == anInterface.getFmtId()).collect(Collectors.toList());
            if(prtclFormats.size()>0){
                //设置协议的归属
                prtclFormats.get(0).setIsPrtclParam(1);
                devInterParam.setInterfacePrtcl(prtclFormats.get(0));
            }
            devInterParam.setDevInterface(anInterface);
            devInterParam.setDevParamList(frameParaInfos.stream().filter(paraInfo -> paraIds.contains(paraInfo.getParaId().toString())).collect(Collectors.toList()));
            devInterParams.add(devInterParam);
        });
        addInterLinkParaMap(devInterParams);
    }

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
                log.error("设备["+baseInfo.getDevName()+"]ip地址或设备编号存在异常，请检查:"+e.getMessage());
            }
        });
    }

    /**
     * @功能：添加参数MAP
     * @param paraList  参数列表
     * @return
     */
    public static void addParaMap(List<FrameParaInfo> paraList) {
        paraList.forEach(paraInfo -> {
            try {
                paramCmdMap.put(ParaHandlerUtil.genLinkKey(paraInfo.getDevType(),paraInfo.getCmdMark()),paraInfo);
                paramNoMap.put(ParaHandlerUtil.genLinkKey(paraInfo.getDevType(),paraInfo.getParaNo()),paraInfo);
            } catch (Exception e) {
                log.error("参数["+ paraInfo.getParaName()+"]的设备类型或命令标识或参数编号存在异常，请检查:"+e.getMessage());
            }
        });
    }

    /**
     * @功能：添加设备类型对应的接口MAP
     * @param interfaces  接口列表
     * @return
     */
    public static void addDevTypeInterMap(List<Interface> interfaces) {
        List<String> devTypes = interfaces.stream().map(Interface::getDevType).collect(Collectors.toList());
        //循环设备类型
        devTypes.forEach(devType->{
            devTypeInterMap.put(devType,interfaces.stream().filter(anInterface -> anInterface.getDevType().equals(devType)).collect(Collectors.toList()));
        });
    }

    /**
     * @功能：添加设备类型对应的参数MAP
     * @param paraList  参数列表
     * @return
     */
    public static void addDevTypeParaMap(List<FrameParaInfo> paraList) {
        List<String> devTypes = paraList.stream().map(FrameParaInfo::getDevType).collect(Collectors.toList());
        //循环设备类型
        devTypes.forEach(devType->{
            devTypeParamMap.put(devType,paraList.stream().filter(paraInfo -> paraInfo.getDevType().equals(devType)).collect(Collectors.toList()));
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
            for (FrameParaInfo paraInfo : devInterParam.getDevParamList()) {
                try {
                    paraInfo.setParaSeq(seq);
                    point = point+Integer.valueOf(paraInfo.getParaByteLen());
                    paraInfo.setParaStartPoint(point);
                } catch (NumberFormatException e) {
                    log.error("参数["+paraInfo.getParaName()+"]的字节长度存在异常，请检查："+e.getMessage());
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
        if(devMap.containsKey(devIPAddr)){
            return devMap.get(devIPAddr);
        }else{
            String rptIpAddr = sysParamService.getParaRemark1(SysConfigConstant.RPT_IP_ADDR);
            if(rptIpAddr.equals(devIPAddr)){
                return genRptBaseInfo();
            }
        }
        throw new BaseException("接收到的IP地址有误,IP地址为:"+devIPAddr+",请检查!");
    }

    /**
     * @功能：获取上报54所 网络连通信息
     * @return  网络连通信息
     */
    public static BaseInfo genRptBaseInfo(){
        BaseInfo rptBaseInfo = new BaseInfo();
        rptBaseInfo.setDevIpAddr(sysParamService.getParaRemark1(SysConfigConstant.RPT_IP_ADDR));
        rptBaseInfo.setDevPort(sysParamService.getParaRemark2(SysConfigConstant.RPT_IP_ADDR));
        rptBaseInfo.setDevNetPtcl(sysParamService.getParaRemark3(SysConfigConstant.RPT_IP_ADDR));
        rptBaseInfo.setIsRptIp("0");
        return rptBaseInfo;
    }
    /**
     * @功能：根据设备类型 获取接口list
     * @param devType   设备类型
     * @return  接口列表
     */
    public static List<Interface> getInterfacesByDevType(String devType) {
        return devTypeInterMap.get(devType) != null ? devTypeInterMap.get(devType) : new ArrayList<>();
    }

    /**
     * @功能：根据设备类型 获取参数list
     * @param devType   设备类型
     * @return  参数列表
     */
    public static List<FrameParaInfo> getParasByDevType(String devType) {
        return devTypeParamMap.get(devType) != null ? devTypeParamMap.get(devType) : new ArrayList<>();
    }

    /**
     * @功能：根据设备编号 获取设备信息
     * @param devNo    设备编号
     * @return  设备对象
     */
    public static BaseInfo getDevInfoByNo(String devNo){
        return devNoMap.get(devNo) != null ? devNoMap.get(devNo) : new BaseInfo();
    }

    /**
     * @功能：根据设备类型 和 命令标识 获取参数信息
     * @param devType  设备类型
     * @param cmrMark  命令标识
     * @return  设备对象
     */
    public static FrameParaInfo getParaInfoByCmd(String devType,String cmrMark){
        FrameParaInfo frameParaInfo = paramCmdMap.get(ParaHandlerUtil.genLinkKey(devType,cmrMark));
        return frameParaInfo !=null ? frameParaInfo : new FrameParaInfo();
    }
    /**
     * @功能：根据 设备类型 和 参数编号 获取参数信息
     * @param devType  设备类型
     * @param ndpaNo   命令标识
     * @return  设备对象
     */
    public static FrameParaInfo getParaInfoByNo(String devType,String ndpaNo){
        FrameParaInfo frameParaInfo = paramNoMap.get(ParaHandlerUtil.genLinkKey(devType,ndpaNo));
        return frameParaInfo !=null ? frameParaInfo : new FrameParaInfo();
    }

    /**
     * @功能：根据设备类型  和  命令标识 获取参数列表
     * @param devType     设备类型
     * @param cmdMark     命令标识
     * @return  接口解析参数列表
     */
    public static List<FrameParaInfo> getInterLinkParaList(String devType,String cmdMark){
        DevInterParam devInterParam = InterLinkParaMap.get(ParaHandlerUtil.genLinkKey(devType,cmdMark));
        if(devInterParam != null){
            return devInterParam.getDevParamList();
        }
        return new ArrayList<>();
    }

    /**
     * @功能：根据设备类型  和  命令标识 获取接口信息
     * @param devType     设备类型
     * @param cmdMark     命令标识
     * @return  接口解析参数列表
     */
    public static Interface getInterLinkInterface(String devType, String cmdMark){
        DevInterParam devInterParam = InterLinkParaMap.get(ParaHandlerUtil.genLinkKey(devType,cmdMark));
        if(devInterParam != null){
            return devInterParam.getDevInterface();
        }
        return new Interface();
    }

    /**
     * @功能：根据设备类型  和  命令标识 获取接口的协议信息
     * @param devType     设备类型
     * @param cmdMark     命令标识
     * @return  接口解析协议
     */
    public static PrtclFormat getPrtclByInterface(String devType, String cmdMark){
        DevInterParam devInterParam = InterLinkParaMap.get(ParaHandlerUtil.genLinkKey(devType,cmdMark));
        if(devInterParam != null){
            return devInterParam.getInterfacePrtcl();
        }
        return new PrtclFormat();
    }

    /**
     * @功能：根据设备类型  和  命令标识 获取参数的协议信息
     * @param devType     设备类型
     * @param cmdMark     命令标识
     * @return  接口解析协议
     */
    public static PrtclFormat getPrtclByPara(String devType, String cmdMark){
        FrameParaInfo frameParaInfo = paramCmdMap.get(ParaHandlerUtil.genLinkKey(devType,cmdMark));
        if (frameParaInfo != null) {
            return frameParaInfo.getInterfacePrtcl();
        }
        return new PrtclFormat();
    }

    /**
     * @功能：根据设备类型  和  命令标识 获取协议信息(协议中包含归属)
     * @param devType     设备类型
     * @param cmdMark     命令标识
     * @return  接口解析协议
     */
    public static PrtclFormat getPrtclByInterfaceOrPara(String devType, String cmdMark){
        PrtclFormat prtclFormat = getPrtclByInterface(devType,cmdMark);
        if(prtclFormat == null){
            prtclFormat = getPrtclByPara(devType,cmdMark);
        }
        return prtclFormat;
    }

    /**
     * @功能：通过设备编号查询同属一个父设备的子设备列表
     * @param devNo   设备编号
     * @return  接口解析协议
     */
    public static List<BaseInfo> getDevsFatByDevNo(String devNo){
        List<BaseInfo> baseInfos = new ArrayList<>();
        BaseInfo baseInfo = getDevInfoByNo(devNo);
        if(baseInfo != null){
            if(!StringUtils.isBlank(baseInfo.getDevParentNo())){
                baseInfos = devNoMap.values().stream().filter(baseInfo1 -> devNo.equals(baseInfo.getDevNo())).collect(Collectors.toList());
            }
        }
        return baseInfos;
    }

    /**
     * @功能：根据设备类型  获取处理类名
     * @param devType     设备类型(参数表中的编码
     * @return  处理类名
     */
    public static String getClassByDevType(String devType){
        return sysParamService.getParaRemark2(devType);
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
        return devNoMap.keySet();
    }

    /**
     * 设备参数转换为帧参数(参数序号、参数下标、设备编号、告警级别除外)
     * @param paraInfos
     * @return
     */
    private static List<FrameParaInfo> changeDevParaToFrame(List<ParaInfo> paraInfos,List<PrtclFormat> prtclList){
        List<FrameParaInfo> frameParaInfos = new ArrayList<>();
        paraInfos.forEach(paraInfo -> {
            FrameParaInfo frameParaInfo = new FrameParaInfo();
            frameParaInfo.setParaId(paraInfo.getNdpaId());  //参数id
            frameParaInfo.setParaNo(paraInfo.getNdpaNo());  //参数编号
            frameParaInfo.setParaCode(paraInfo.getNdpaCode());  //参数编码
            frameParaInfo.setParaName(paraInfo.getNdpaName());  //参数名称
            frameParaInfo.setCmdMark(paraInfo.getNdpaCmdMark()); //命令标识
            frameParaInfo.setParaByteLen(paraInfo.getNdpaByteLen());  // 字节长度
            Map map = new HashMap();
            if(!StringUtils.isBlank(paraInfo.getNdpaSelectData())){
                JSONArray.parseArray(paraInfo.getNdpaSelectData(), ParaSpinnerInfo.class).forEach(paraSpinnerInfo -> map.put(paraSpinnerInfo.getCode(),paraSpinnerInfo.getName()));
            }
            frameParaInfo.setSelectMap(map);//下拉值域
            frameParaInfo.setDevType(paraInfo.getDevType());      //设备类型
            frameParaInfo.setDevTypeCode(paraInfo.getDevTypeCode());      //设备类型编码
            frameParaInfo.setNdpaAccessRight(paraInfo.getNdpaAccessRight()); //访问权限
            List<PrtclFormat> prtclFormats = prtclList.stream().filter(prtclFormat -> prtclFormat.getFmtId() == paraInfo.getFmtId()).collect(Collectors.toList());
            if(prtclFormats.size()>0){
                //设置协议归属
                prtclFormats.get(0).setIsPrtclParam(0);
                frameParaInfo.setInterfacePrtcl(prtclFormats.get(0));      //解析协议
            }
            frameParaInfo.setTransRule(paraInfo.getNdpaTransRule()); //内外转换值域
            frameParaInfo.setAlertPara(paraInfo.getNdpaAlertPara()); //字段类型
            frameParaInfos.add(frameParaInfo);
        });
        return frameParaInfos;
    }

}
