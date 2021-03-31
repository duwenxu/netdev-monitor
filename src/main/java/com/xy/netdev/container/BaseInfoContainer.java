package com.xy.netdev.container;

import com.alibaba.fastjson.JSONArray;
import com.xy.common.exception.BaseException;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.ParaHandlerUtil;
import com.xy.netdev.monitor.bo.DevInterParam;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.bo.ParaSpinnerInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.monitor.entity.PrtclFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;
import static com.xy.netdev.common.constant.SysConfigConstant.*;
import static com.xy.netdev.monitor.constant.MonitorConstants.SUB_KU_GF;
import static com.xy.netdev.monitor.constant.MonitorConstants.SUB_MODEM;

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
     * 参数map K设备序号 V接口(类型为页面查询接口)
     */
    private static Map<String, Interface> devPageItfMap = new HashMap<>();

    /**
     * 参数map K设备序号 V接口(类型为组装控制接口)
     */
    private static Map<String, Interface> devAssConItfMap = new HashMap<>();

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
        //加载页面查询接口和组合控制接口缓存
        addDevNoItfMap(devs,interfaces);
        //加载设备接口参数信息
        //获取非子接口
        List<Interface> iftParents = interfaces.stream().filter(anInterface -> anInterface.getItfParentId() == null).collect(Collectors.toList());
        //获取子接口列表
        List<Interface> subParents = interfaces.stream().filter(anInterface -> !iftParents.contains(anInterface)).collect(Collectors.toList());
        addInterLinkParaMap(genDevInterParam(iftParents,subParents,frameParaInfos,prtclList));
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
     * @功能：添加设备序号对应的页面查询接口
     * @param devList  设备列表
     * @param interfaces  接口列表
     * @return
     */
    public static void addDevNoItfMap(List<BaseInfo> devList, List<Interface> interfaces) {
        devList.forEach(baseInfo -> {
            try {
                List<Interface> pageInterfaces = interfaces.stream().filter(anInterface -> baseInfo.getDevType().equals(anInterface.getDevType()) && INTERFACE_TYPE_PAGE_QUERY.equals(anInterface.getItfType())).collect(Collectors.toList());
                if(pageInterfaces.size() > 0){
                    devPageItfMap.put(baseInfo.getDevNo(),pageInterfaces.get(0));
                }
                List<Interface> ctrlInterfaces = interfaces.stream().filter(anInterface -> baseInfo.getDevType().equals(anInterface.getDevType()) && INTERFACE_TYPE_PACK_CTRL.equals(anInterface.getItfType())).collect(Collectors.toList());
                if(ctrlInterfaces.size() >0){
                    devAssConItfMap.put(baseInfo.getDevNo(),ctrlInterfaces.get(0));
                }
            } catch (Exception e) {
                log.error("设备["+baseInfo.getDevName()+"]设备编号存在异常，请检查:"+e.getMessage());
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
            int seq = 0;
            int point = 0 ;
            for (FrameParaInfo paraInfo : devInterParam.getDevParamList()) {
                try {
                    seq++;
                    paraInfo.setParaSeq(seq);  //参数序号
                    //对于存在分隔符的参数下标做特殊处理
                    String devType = paraInfo.getDevType();
                    if (SUB_MODEM.equals(devType)||SUB_KU_GF.equals(devType)){
                        if (seq == 1){
                            point =point +1;
                        }else {
                            point = point+2;
                        }
                    }
                    paraInfo.setParaStartPoint(point);//参数下标：从哪一个字节开始
                    log.debug("cmd:{}----point:{}",paraInfo.getCmdMark(),point);
                    String byteLen = StringUtils.isBlank(paraInfo.getParaByteLen()) ? "0":paraInfo.getParaByteLen();
                    point = point+Integer.valueOf(byteLen);
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
            String rptIpAddr = sysParamService.getParaRemark1(RPT_IP_ADDR);
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
        rptBaseInfo.setDevIpAddr(sysParamService.getParaRemark1(RPT_IP_ADDR));
        rptBaseInfo.setDevPort(sysParamService.getParaRemark2(RPT_IP_ADDR));
        rptBaseInfo.setDevNetPtcl(sysParamService.getParaRemark3(RPT_IP_ADDR));
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
     * @功能：根据设备序号 获取页面查询接口
     * @param devNo   设备序号
     * @return  接口列表
     */
    public static Interface getPageItfInfo(String devNo) {
        return devPageItfMap.get(devNo) != null ? devPageItfMap.get(devNo) : new Interface();
    }

    /**
     * @功能：根据设备序号 获取组装控制接口
     * @param devNo   设备序号
     * @return  接口列表
     */
    public static Interface getCtrlItfInfo(String devNo) {
        return devAssConItfMap.get(devNo) != null ? devAssConItfMap.get(devNo) : new Interface();
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
     * @功能：根据设备类型  和  命令标识 查询子接口列表
     * @param devType     设备类型
     * @param cmdMark     命令标识
     * @return  接口解析参数列表
     */
    public static List<DevInterParam> getSubIftList(String devType,String cmdMark){
        DevInterParam devInterParam = InterLinkParaMap.get(ParaHandlerUtil.genLinkKey(devType,cmdMark));
        if(devInterParam != null){
            return devInterParam.getSubItfList();
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
        if(prtclFormat.getFmtId() == null||StringUtils.isEmpty(prtclFormat.getFmtId()+"")){
            prtclFormat = getPrtclByPara(devType,cmdMark);
        }
        return prtclFormat;
    }

    /**
     * @功能：通过设备编号查询同属一个父设备的子设备列表
     * @param devNo   设备编号
     * @return  接口解析协议
     */
    public static List<BaseInfo> getDevsFatByDevNo(String devNo) {
        List<BaseInfo> baseInfos = new ArrayList<>();
        //获取父设备
        String parId = getDevInfoByNo(devNo).getDevParentNo();
        if (!StringUtils.isBlank(parId)) {
            baseInfos = devNoMap.values().stream().filter(baseInfo -> parId.equals(baseInfo.getDevParentNo()))
                    .filter(base -> DEV_DEPLOY_MASTER.equals(base.getDevDeployType()) || DEV_DEPLOY_SLAVE.equals(base.getDevDeployType()))
                    .collect(Collectors.toList());
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
     * 根据 标识字 和 协议信息 获取操作类型
     *
     * @param prtclFormat 协议
     * @param sign        标识字
     * @return 操作类型
     */
    public static String getOptByPrtcl(PrtclFormat prtclFormat, String sign) {
        if (prtclFormat == null || StringUtils.isEmpty(String.valueOf(prtclFormat.getFmtId())) || StringUtils.isEmpty(sign)) {
            log.error("getOptByPrtcl方法执行异常:协议格式对象或标识字为空");
            return null;
        }
        if (sign.equals(prtclFormat.getFmtSkey())) {
            return OPREATE_QUERY;
        } else if (sign.equals(prtclFormat.getFmtCkey())) {
            return OPREATE_CONTROL;
        } else if (sign.equals(prtclFormat.getFmtSckey())) {
            return OPREATE_QUERY_RESP;
        } else if (sign.equals(prtclFormat.getFmtCckey())) {
            return OPREATE_CONTROL_RESP;
        } else {
            log.error("getOptByPrtcl方法执行异常:未匹配到对应的操作类型");
            return null;
        }
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
            frameParaInfo.setDataType(paraInfo.getNdpaDatatype());//数值类型
            frameParaInfo.setNdpaRemark1Desc(paraInfo.getNdpaRemark1Desc());//备注1
            frameParaInfo.setNdpaRemark1Data(paraInfo.getNdpaRemark1Data());//数据1
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
            frameParaInfo.setAlertPara(paraInfo.getNdpaAlertPara()); //状态上报类型
            frameParaInfo.setAlertLevel(paraInfo.getNdpaAlertLevel());
            frameParaInfo.setSubParaList(new ArrayList<>());
            if(paraInfo.getNdpaCmplexLevel().equals(PARA_COMPLEX_LEVEL_SUB)){
                frameParaInfos.stream().filter(paraInfo1 -> paraInfo.getNdpaParentNo().equals(paraInfo1.getParaNo()) && paraInfo.getDevType().equals(paraInfo1.getDevType())).collect(Collectors.toList()).forEach(frameParaInfo1 -> {
                    frameParaInfo1.addSubPara(frameParaInfo);
                });
            }else{
                frameParaInfos.add(frameParaInfo);
            }
        });
        return frameParaInfos;
    }

    /**
     * 生成设备接口参数实体list（运用递归方式处理组装接口）
     * @param parentItfs  所有的上级接口
     * @param subItfs     所有的子接口
     * @param frameParaInfos
     * @param prtclList
     * @return
     */
    private static List<DevInterParam> genDevInterParam(List<Interface> parentItfs,List<Interface> subItfs,List<FrameParaInfo> frameParaInfos,List<PrtclFormat> prtclList){
        List<DevInterParam> devInterParams = new ArrayList<>();
        parentItfs.forEach(anInterface -> {
            DevInterParam devInterParam = new DevInterParam();
            //id
            devInterParam.setId(ParaHandlerUtil.genLinkKey(anInterface.getDevType(),anInterface.getItfCmdMark()));
            //接口
            devInterParam.setDevInterface(anInterface);
            //协议
            List<PrtclFormat> prtclFormats = prtclList.stream().filter(prtclFormat -> prtclFormat.getFmtId() == anInterface.getFmtId()).collect(Collectors.toList());
            if(prtclFormats.size()>0){
                //设置协议的归属
                prtclFormats.get(0).setIsPrtclParam(1);
                devInterParam.setInterfacePrtcl(prtclFormats.get(0));
            }
            //参数列表
            List<String> paraIds = StringUtils.isBlank(anInterface.getItfDataFormat())? new ArrayList<>():Arrays.asList(anInterface.getItfDataFormat().split(","));
            //参数list
            devInterParam.setDevParamList(frameParaInfos.stream().filter(paraInfo -> paraIds.contains(paraInfo.getParaId().toString()))
                    //此处需要倒叙则增加使用.sorted(Comparator.comparing(FrameParaInfo::getParaNo).reversed())
                    .sorted(Comparator.comparing(FrameParaInfo::getParaNo))
                    .collect(Collectors.toList()));
            //如果为组合接口则填充子接口列表：递归方法
            List<DevInterParam> subList = new ArrayList<>();
            if(INTERFACE_TYPE_PACK_QUERY.equals(anInterface.getItfType())){
                //查找所属当前接口的所有子接口
                List<Interface> interfaceList = subItfs.stream().filter(anInterface1 -> anInterface.getDevType().equals(anInterface1.getDevType()) && anInterface.getItfId().equals(anInterface1.getItfParentId())).collect(Collectors.toList());
                //开始递归
                subList = genDevInterParam(interfaceList,subItfs,frameParaInfos,prtclList);
            }
            devInterParam.setSubItfList(subList);
            devInterParams.add(devInterParam);

        });
        return devInterParams;
    }
}
