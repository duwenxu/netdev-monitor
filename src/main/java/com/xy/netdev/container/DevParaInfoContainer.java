package com.xy.netdev.container;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Charsets;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.DateTools;
import com.xy.netdev.common.util.ParaHandlerUtil;
import com.xy.netdev.container.paraext.ParaExtServiceFactory;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.snmp.SnmpRptDTO;
import com.xy.netdev.monitor.bo.DevStatusInfo;
import com.xy.netdev.monitor.bo.ParaSpinnerInfo;
import com.xy.netdev.monitor.bo.ParaViewInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.synthetical.util.SyntheticalUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import static com.xy.netdev.common.constant.SysConfigConstant.*;
import static com.xy.netdev.monitor.constant.MonitorConstants.INT;

/**
 * <p>
 * 各设备参数缓存容器类
 * </p>
 *
 * @author tangxl
 * @since 2021-03-08
 */
@Slf4j
public class DevParaInfoContainer {

    private static ISysParamService sysParamService;

    /**
     * 设备参数MAP K设备编号  V设备参数信息
     */
    private static final Map<String, Map<String, ParaViewInfo>> devParaMap = new LinkedHashMap<>();

    /**
     * SNMP OID-参数信息 映射  结构： <DevNo,<OID,SnmpReqDTO>>
     * 用来存储Snmp上报参数信息
     */
    @Getter
    private static final Map<String, Map<String, SnmpRptDTO>> devSnmpParaMap = new ConcurrentHashMap<>(10);

    /**
     * 设备编号 与 设备状态OID映射   结构：<DevNo,Set<devStatusOid>>
     * Acu包含3个设备上报状态，因此用Set存储
     */
    @Getter
    private static final Map<String,Set<String>> devNoStatusOidMap = new ConcurrentHashMap<>(10);
    /**
     * 综合上报参数MAP K设备参数OID  V设备参数信息
     */
    private static final Map<String, ParaInfo> devParaOidMap = new HashMap<>();
    /**
     * 综合上报参数MAP K设备状态参数OID  V设备编号
     */
    private static final Map<String, String> devStatusOidMapDevNo = new HashMap<>();

    public static Map<String, String> getDevStatusOidMapDevNo() {
        return devStatusOidMapDevNo;
    }

    /**
     * 设备响应次数
     */
    public static int respNum = 0;


    public static Map<String, ParaInfo> getDevParaOidMap() {
        return devParaOidMap;
    }


    /**
     * @param paraList 参数列表
     * @return
     * @功能：添加设备参数MAP
     */
    public static void initData(List<ParaInfo> paraList,ISysParamService sysParamService) {
        DevParaInfoContainer.sysParamService = sysParamService;
        Map<String, List<ParaInfo>> paraMapByDevType = paraList.stream().collect(Collectors.groupingBy(ParaInfo::getDevType));
        BaseInfoContainer.getDevNos().forEach(devNo -> {
            String devType = BaseInfoContainer.getDevInfoByNo(devNo).getDevType();
            devParaMap.put(devNo, assembleViewList(devNo, paraMapByDevType.get(devType)));
            ParaExtServiceFactory.genParaExtService(devType).setCacheDevParaViewInfo(devNo);
        });
        //SNMP 内存参数映射处理
        initSnmpRptData();
    }

    private static void initSnmpRptData() {
        initSnmpRptData(devParaMap);
    }

    /**
     * 初始化SNMP上报数据
     * 从指定的参数值缓存更新SNMP的缓存数据
     *
     * @param devParaMap 设备参数值缓存
     */
    private static void initSnmpRptData(Map<String, Map<String, ParaViewInfo>> devParaMap) {
        long t1 = System.currentTimeMillis();
        for (Map.Entry<String, Map<String, ParaViewInfo>> entry : devParaMap.entrySet()) {
            String currentDevNo = entry.getKey();
            String devStatus = BaseInfoContainer.getDevInfoByNo(currentDevNo).getDevStatus();
            if (!DEV_STATUS_NEW.equals(devStatus)){ continue;}
            Collection<ParaViewInfo> values = entry.getValue().values();
            //遍历筛选snmp参数添加缓存初始化
            for (ParaViewInfo paraView : values) {
                List<ParaViewInfo> subParaList = paraView.getSubParaList();
                if (subParaList == null || subParaList.size() == 0) {
                    addSnmpParaData(currentDevNo, paraView);
                } else {
                    for (ParaViewInfo subPara : subParaList) {
                        addSnmpParaData(currentDevNo, subPara);
                    }
                }
            }
        }
        log.debug("初始化SNMP数据耗时：[{}]", System.currentTimeMillis() - t1);
    }

    /**
     * 添加单个SNMP数据映射到缓存
     *
     * @param currentDevNo 设备编号
     * @param value        参数值结构体
     */
    private static void addSnmpParaData(String currentDevNo, ParaViewInfo value) {
        //参数是否需要上报的条件：1.提供给54所访问 2.上报oid不为空
        boolean canBeOpt = SysConfigConstant.IS_DEFAULT_TRUE.equals(value.getNdpaOutterStatus()) && !StringUtils.isEmpty(value.getRptOidSign());
        if (canBeOpt) {
            //初始化尚未加入的设备缓存
            if (!devSnmpParaMap.containsKey(currentDevNo)) {
                devSnmpParaMap.put(currentDevNo, new ConcurrentHashMap<>(10));
            }
            Map<String, SnmpRptDTO> viewInfoMap = devSnmpParaMap.get(currentDevNo);
            //按上报协议规则拼装参数OID
            String rptOid = SyntheticalUtil.genRptOid(value.getRptOidSign(), value.getParaCode(), sysParamService);
            SnmpRptDTO snmpRptDTO = new SnmpRptDTO();
            BeanUtils.copyProperties(value, snmpRptDTO);
            //将 oid-snmp数据上报结构体 映射加入缓存
            viewInfoMap.put(rptOid, snmpRptDTO);
        }
    }

    /**
     * SNMP综合网管上报---OID参数后缀  区号+站号+设备编号
     */
    public static final String SNMP_RPT_SUFFIX = ".1.1.1";

    /**
     * 初始化各个设备固定参数值 如：设备连接状态等
     */
    public static void initSnmpDevStatusRptData() {
        for (Map.Entry<String, Map<String, SnmpRptDTO>> snmpMap : devSnmpParaMap.entrySet()) {
            String currentDevNo = snmpMap.getKey();
//            String oid0 = new ArrayList<>(snmpMap.getValue().entrySet()).get(0).getKey();
//            String oidPrefix = oid0.substring(0, oid0.lastIndexOf(SNMP_RPT_SUFFIX));

            Set<String> oidPrefixes = snmpMap.getValue().keySet().stream().map(oid -> oid.substring(0, oid.lastIndexOf(SNMP_RPT_SUFFIX))).collect(Collectors.toSet());
            for (String oidPrefix:oidPrefixes){
                oidPrefix = oidPrefix.substring(0, oidPrefix.lastIndexOf("."));
                /**区号*/
                String devOid1 = oidPrefix + ".1" + SNMP_RPT_SUFFIX;
                /**站号*/
                String devOid2 = oidPrefix + ".2" + SNMP_RPT_SUFFIX;
                /**设备编号*/
                String devOid3 = oidPrefix + ".3" + SNMP_RPT_SUFFIX;
                /**根据MIB库定义  设备连接状态在各个设备中：均为1.1.4*/
                String devOid4 = oidPrefix + ".4" + SNMP_RPT_SUFFIX;
                /**获取设备状态参数*/
                String oidDevNo = DevParaInfoContainer.getOidDevNo(devOid4);
                DevStatusInfo devStatusInfo = DevStatusContainer.getDevStatusInfo(oidDevNo);
                String isInterrupt = devStatusInfo.getIsInterrupt();
                String isConnected = SNMP_DEV_STATUS_UN_CONNECTED;
                if (RPT_DEV_STATUS_UN_INTERRUPT.equals(isInterrupt)) {
                    isConnected = SNMP_DEV_STATUS_CONNECTED;
                }
                SnmpRptDTO rptDTO4 = SnmpRptDTO.builder().paraCode("4").paraName("设备连接状态").paraDatatype(INT).paraVal(isConnected).build();
                SnmpRptDTO rptDTO1 = SnmpRptDTO.builder().paraCode("1").paraName("区号").paraDatatype(INT).paraVal("1").build();
                SnmpRptDTO rptDTO2 = SnmpRptDTO.builder().paraCode("2").paraName("站号").paraDatatype(INT).paraVal("1").build();
                SnmpRptDTO rptDTO3 = SnmpRptDTO.builder().paraCode("3").paraName("设备编号").paraDatatype(INT).paraVal("1").build();
                devSnmpParaMap.get(currentDevNo).put(devOid4, rptDTO4);
                devSnmpParaMap.get(currentDevNo).put(devOid1, rptDTO1);
                devSnmpParaMap.get(currentDevNo).put(devOid2, rptDTO2);
                devSnmpParaMap.get(currentDevNo).put(devOid3, rptDTO3);
                //此处的set用来处理Acu下包含了三个设备的参数及连接信息上报(Acu、Ka40W功放、Ku100W功放)，需要在这里Acu设备下添加这三个上报信息
                if (!devNoStatusOidMap.containsKey(currentDevNo)){
                    devNoStatusOidMap.put(currentDevNo,new CopyOnWriteArraySet());
                }
                devNoStatusOidMap.get(currentDevNo).add(devOid4);
            }
        }
    }

    /**
     * @param devNo           设备编号
     * @param devTypeParaList 设备类型参数列表
     * @return 设备显示列表
     * @功能：根据设备类型对应的参数信息 生成设备显示列表
     */
    private static Map<String, ParaViewInfo> assembleViewList(String devNo, List<ParaInfo> devTypeParaList) {
        //此处改为LinkedHashMap为了排序
        Map<String, ParaViewInfo> paraViewMap = new LinkedHashMap<>();
        if (devTypeParaList != null && !devTypeParaList.isEmpty()) {
            //按照参数显示顺序字段排序
            devTypeParaList.sort(Comparator.comparing(ParaInfo::getNdpaShowSeq,Comparator.nullsLast(Integer::compareTo)));
            for (ParaInfo paraInfo : devTypeParaList) {
                //确保复杂参数在子参数之前被添加
                if (!paraInfo.getNdpaCmplexLevel().equals(SysConfigConstant.PARA_COMPLEX_LEVEL_SUB)) {
                    paraViewMap.put(ParaHandlerUtil.genLinkKey(devNo, paraInfo.getNdpaNo()), genParaViewInfo(devNo, paraInfo));
                }
            }
            for (ParaInfo paraInfo : devTypeParaList) {
                genOidMap(devNo, paraInfo);
                if (paraInfo.getNdpaCmplexLevel().equals(SysConfigConstant.PARA_COMPLEX_LEVEL_SUB)) {
                    paraViewMap.get(ParaHandlerUtil.genLinkKey(devNo, paraInfo.getNdpaParentNo())).addSubPara(genParaViewInfo(devNo, paraInfo));
                }
            }
        }
        return paraViewMap;
    }

    /**
     * 初始化中的snmp参数缓存填充
     * @param devNo 设备编号
     * @param paraInfo 参数信息
     */
    private static void genOidMap(String devNo, ParaInfo paraInfo) {
        //对需要上报的参数
        if (paraInfo.getNdpaOutterStatus().equals(SysConfigConstant.IS_DEFAULT_TRUE) && !StringUtils.isEmpty(paraInfo.getNdpaRptOid())) {
            paraInfo.setDevNo(devNo);
            String oid = SyntheticalUtil.genRptOid(paraInfo.getNdpaRptOid(), paraInfo.getNdpaCode(), sysParamService);
            //加入缓存
            devParaOidMap.put(oid, paraInfo);
            //扩展添加设备状态等oid
            genStdOidPara(oid, devNo);
        }
    }

    private static void genStdOidPara(String oid, String devNo) {
        String oidPrefix = oid.substring(0, oid.lastIndexOf(SNMP_RPT_SUFFIX));
        oidPrefix = oidPrefix.substring(0, oidPrefix.lastIndexOf("."));
        String devOid1 = oidPrefix + ".1" + SNMP_RPT_SUFFIX;
        String devOid2 = oidPrefix + ".2" + SNMP_RPT_SUFFIX;
        String devOid3 = oidPrefix + ".3" + SNMP_RPT_SUFFIX;
        String devOid4 = oidPrefix + ".4" + SNMP_RPT_SUFFIX;
        ParaInfo paraInfo = new ParaInfo();
        paraInfo.setNdpaDatatype(SysConfigConstant.PARA_DATA_TYPE_INT);
        if (!devParaOidMap.containsKey(devOid1)) {
            devParaOidMap.put(devOid1, paraInfo);
        }
        if (!devParaOidMap.containsKey(devOid2)) {
            devParaOidMap.put(devOid2, paraInfo);
        }
        if (!devParaOidMap.containsKey(devOid3)) {
            devParaOidMap.put(devOid3, paraInfo);
        }
        if (!devParaOidMap.containsKey(devOid4)) {
            devParaOidMap.put(devOid4, paraInfo);
            devStatusOidMapDevNo.put(devOid4, devNo);
        }
    }

    private static ParaViewInfo genParaViewInfo(String devNo, ParaInfo paraInfo) {
        ParaViewInfo viewInfo = new ParaViewInfo();
        viewInfo.setParaId(paraInfo.getNdpaId());
        viewInfo.setParaNo(paraInfo.getNdpaNo());
        viewInfo.setParaCode(paraInfo.getNdpaCode());
        viewInfo.setParaName(paraInfo.getNdpaName());
        viewInfo.setParaCmdMark(paraInfo.getNdpaCmdMark());
        viewInfo.setAccessRight(paraInfo.getNdpaAccessRight());
        viewInfo.setParaUnit(paraInfo.getNdpaUnit());
        viewInfo.setParaDatatype(paraInfo.getNdpaDatatype());
        viewInfo.setParaSimpleDatatype(sysParamService.getParaRemark3(paraInfo.getNdpaDatatype()));
        viewInfo.setParaStrLen(paraInfo.getNdpaStrLen());
        viewInfo.setParahowMode(paraInfo.getNdpaShowMode());
        viewInfo.setParaValMax1(paraInfo.getNdpaValMax1());  //最大值1
        viewInfo.setParaValMin1(paraInfo.getNdpaValMin1());  //最小值1
        viewInfo.setParaValMax2(paraInfo.getNdpaValMax2());  //最大值2
        viewInfo.setParaValMin2(paraInfo.getNdpaValMin2());  //最小值2
        viewInfo.setParaValStep(paraInfo.getNdpaValStep());
        viewInfo.setParaSpellFmt(paraInfo.getNdpaSpellFmt());
        viewInfo.setParaViewFmt(paraInfo.getNdpaViewFmt());
        viewInfo.setParaCmplexLevel(paraInfo.getNdpaCmplexLevel());
        viewInfo.setParaVal(paraInfo.getNdpaDefaultVal());
        viewInfo.setSubParaLinkType(paraInfo.getNdpaLinkType());
        viewInfo.setSubParaLinkCode(paraInfo.getNdpaLinkCode());
        viewInfo.setSubParaLinkVal(paraInfo.getNdpaLinkVal());
        viewInfo.setDevNo(devNo);
        viewInfo.setDevType(paraInfo.getDevType());
        viewInfo.setParaCmdMark(paraInfo.getNdpaCmdMark());
        viewInfo.setSpinnerInfoList(JSONArray.parseArray(paraInfo.getNdpaSelectData(), ParaSpinnerInfo.class));
        //acu选择预置卫星特殊处理  sunchao
        if(BaseInfoContainer.getDevTypeOptSat().contains(paraInfo.getDevType()) && "optSate".equals(paraInfo.getNdpaCmdMark()) && BaseInfoContainer.getSpacePresets().size()>0){
            List<ParaSpinnerInfo> spinnerInfos = new ArrayList<>();
            BaseInfoContainer.getSpacePresets().forEach(ntdvSpacePreset -> {
                ParaSpinnerInfo paraSpinnerInfo = new ParaSpinnerInfo();
                paraSpinnerInfo.setCode(ntdvSpacePreset.getSpId().toString());
                paraSpinnerInfo.setName(ntdvSpacePreset.getSpName()+"["+sysParamService.getParaName(ntdvSpacePreset.getSpPolarization())+"]");
                spinnerInfos.add(paraSpinnerInfo);
            });
            viewInfo.setSpinnerInfoList(spinnerInfos);
            //设置缺省值：默认第一个卫星
            viewInfo.setParaVal(spinnerInfos.get(0).getCode());
        }
        viewInfo.setParaByteLen(paraInfo.getNdpaByteLen());
        viewInfo.setNdpaOutterStatus(paraInfo.getNdpaOutterStatus());
        viewInfo.setNdpaIsImportant(paraInfo.getNdpaIsImportant());
        viewInfo.setNdpaIsImportant(paraInfo.getNdpaIsImportant());
        viewInfo.setRptOidSign(paraInfo.getNdpaRptOid());
        return viewInfo;
    }


    /**
     * @param devNo 设备编号
     * @return 设备显示参数列表
     * @功能：根据设备显示参数列表
     */
    public static List<ParaViewInfo> getDevParaViewList(String devNo) {
        String devType = BaseInfoContainer.getDevInfoByNo(devNo).getDevType();
        return ParaExtServiceFactory.genParaExtService(devType).getCacheDevParaViewInfo(devNo);
    }

    /**
     * @param devNo 设备编号
     * @return 设备显示参数列表
     * @功能：根据设备显示参数列表
     */
    public static List<ParaViewInfo> getDevParaExtViewList(String devNo) {
        return new ArrayList(devParaMap.get(devNo).values());
    }

    /**
     * @param devNo  设备编号
     * @param paraNo 参数编号
     * @return 设备参数显示信息
     * @功能：根据设备编号和参数编号 返回参数显示信息
     */
    public static ParaViewInfo getDevParaView(String devNo, String paraNo) {
        return devParaMap.get(devNo).get(ParaHandlerUtil.genLinkKey(devNo, paraNo));
    }

    private static final Map<String, Map<String, ParaViewInfo>> changedDevParaMap = new LinkedHashMap<>(10);

    /**
     * @param respData 协议解析响应数据
     * @return 数据是否发生变化
     * @功能：设置设备响应参数信息
     */
    public synchronized static boolean handlerRespDevPara(FrameRespData respData) {
        //ACU参数一直不停变化，需要特殊处理上报
        /*if (respData.getDevType().equals(SysConfigConstant.DEVICE_ACU) || respData.getDevType().equals(SysConfigConstant.DEVICE_ACU_SAN)) {
            respNum++;
            if (respNum % 15 == 0) {
                respNum = 0;
            } else {
                return false;
            }
        }*/
        List<FrameParaData> frameParaList = respData.getFrameParaList();
        int num = 0;
        if (frameParaList != null && !frameParaList.isEmpty()) {
            for (FrameParaData frameParaData : frameParaList) {
                String devNo = respData.getDevNo();
                String paraNo = frameParaData.getParaNo();
                String linkKey = ParaHandlerUtil.genLinkKey(devNo, paraNo);
                ParaViewInfo paraViewInfo;
                //空指针异常处理
                try {
                    paraViewInfo = devParaMap.get(devNo).get(linkKey);
                } catch (Exception e) {
                    continue;
                }
                if (paraViewInfo != null && StringUtils.isNotEmpty(frameParaData.getParaVal()) && !frameParaData.getParaVal().equals(paraViewInfo.getParaVal())) {
                    //对一些特定规则的snmp协议参数值做扩展处理
                    snmpParamValExt(paraViewInfo,frameParaData);
                    //snmp存储参数值变更
                    updateChanged(devNo,linkKey,paraViewInfo,frameParaData.getParaVal());
                    paraViewInfo.setParaVal(frameParaData.getParaVal());
                    paraViewInfo.setParaOrigByte(frameParaData.getParaOrigByte());
                    //组合参数修改子参数值
                    if (paraViewInfo.getParaCmplexLevel().equals(PARA_COMPLEX_LEVEL_COMPOSE)) {
                        for (ParaViewInfo paraViewInfo1 : paraViewInfo.getSubParaList()) {
                            List<FrameParaData> lists = frameParaList.stream().filter(frameParaData1 -> frameParaData1.getParaNo().equals(paraViewInfo1.getParaNo())).collect(Collectors.toList());
                            if(lists.size() > 0){
                                FrameParaData frameParaData1 = lists.get(0);
                                snmpParamValExt(paraViewInfo1,frameParaData1);
                                paraViewInfo1.setParaVal(frameParaData1.getParaVal());
                                paraViewInfo1.setParaOrigByte(frameParaData1.getParaOrigByte());
                            }
                        }
                    }
                    num++;
                }
                //功放设备的特殊处理
                if (SysConfigConstant.DEVICE_CAR_GF.equals(frameParaData.getDevType()) && "15".equals(frameParaData.getParaNo())) {
                    DevStatusContainer.setMasterOrSlave(frameParaData.getDevNo());
                }
            }
        }
        //更新值改变的Snmp协议需要上报的数据信息
        updateSnmpRptData();
        return num > 0;
    }

    /***
     * @Description  对于snmp协议中的部分参数做特殊解析
     * @Param [paraView, frameParaData] [内存中的参数信息对象，相应数据解析的参数信息对象]
     * @return void
     **/
    @SneakyThrows
    private synchronized static void snmpParamValExt(ParaViewInfo paraView, FrameParaData frameParaData) {
        String devNo = frameParaData.getDevNo();
        BaseInfo base = BaseInfoContainer.getDevInfoByNo(devNo);
        String paraVal = frameParaData.getParaVal();
        String paraCmplexLevel = paraView.getParaCmplexLevel();
        //对于snmp协议中的16进制结果做处理
        boolean ifNeedParse = SNMP.equals(base.getDevNetPtcl()) && paraVal != null && match(paraVal) && (PARA_COMPLEX_LEVEL_SIMPLE.equals(paraCmplexLevel) || PARA_COMPLEX_LEVEL_SUB.equals(paraCmplexLevel));
        if (ifNeedParse) {
            String datatype = paraView.getParaDatatype();
            if (PARA_DATA_TYPE_STR.equals(datatype)) {
                String newVal = null;
                try {
                    byte[] bytes = HexUtil.decodeHex(paraVal.replace(":", ""));
                    //AscII码直接转换为对应值
                    newVal = StrUtil.str(bytes, Charsets.UTF_8);
                } catch (Exception e) {
                    log.warn("String类型16进制转bytes异常，设备编号：[{}],参数编号：[{}],参数名称:[{}],参数值：[{}]", devNo, paraView.getParaNo(), paraView.getParaName(), paraVal);
                }
                frameParaData.setParaVal(newVal);
            }
        }
    }

    /**
     * 匹配指定条件的数据
     * @param data 数据
     * @return 是否非日期但由:分隔
     */
    private static boolean match(String data){
        return data.contains(":") && !DateTools.isValidDate(data);
    }

    /**
     * 更新改变的SNMP参数值
     */
    private static void updateSnmpRptData() {
        initSnmpRptData(changedDevParaMap);
    }

    /**
     * 更新改动的参数值缓存
     */
    private static void updateChanged(String devNo, String linkKey, ParaViewInfo paraViewInfo, String val) {
        if (!changedDevParaMap.containsKey(devNo)){
            changedDevParaMap.put(devNo, new ConcurrentHashMap<>());
        }
        paraViewInfo.setParaVal(val);
        changedDevParaMap.get(devNo).put(linkKey,paraViewInfo);
    }

    /**
     * 更新缓存devParaMap参数值
     */
    public static void updateParaValue(String devNo, String linkKey, String val) {
        if (devParaMap.containsKey(devNo)){
            if(devParaMap.get(devNo).containsKey(linkKey)){
                ParaViewInfo paraViewInfo = devParaMap.get(devNo).get(linkKey);
                paraViewInfo.setParaVal(val);
                devParaMap.get(devNo).put(linkKey,paraViewInfo);
                /*//刷新缓存
                DevIfeMegSend.sendParaToDev(devNo);*/
            }
        }
    }

    /**
     * @param oid 设备参数OID
     * @return 设备参数信息
     * @功能：根据设备OID 返回参数信息
     */
    public static ParaInfo getOidParaIno(String oid) {
        return devParaOidMap.get(oid);
    }

    /**
     * @param oid 设备参数OID
     * @return true 存在 false 不存在
     * @功能：判断是否存在该OID
     */
    public static boolean containsOid(String oid) {
        return devParaOidMap.containsKey(oid);
    }

    /**
     * @param oid 设备参数OID
     * @return 设备参数信息
     * @功能：根据设备OID 返回参数信息
     */
    public static String getOidDevNo(String oid) {
        return devStatusOidMapDevNo.get(oid);
    }

    /**
     * 清空缓存
     */
    public static void cleanCache() {
        devParaMap.clear();
        devParaOidMap.clear();
    }

    public static void main(String[] args) {
        String oid = "1.2.3";
        System.out.println(oid.substring(0, oid.lastIndexOf(".")));
        System.out.println(oid.substring(oid.lastIndexOf(".") + 1));
    }
}
