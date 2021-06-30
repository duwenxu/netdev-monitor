package com.xy.netdev.container;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.ParaHandlerUtil;
import com.xy.netdev.container.paraext.ParaExtServiceFactory;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.snmp.SnmpRptDTO;
import com.xy.netdev.monitor.bo.DevStatusInfo;
import com.xy.netdev.monitor.bo.ParaSpinnerInfo;
import com.xy.netdev.monitor.bo.ParaViewInfo;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.synthetical.util.SyntheticalUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.xy.netdev.common.constant.SysConfigConstant.PARA_COMPLEX_LEVEL_COMPOSE;
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
     * SNMP OID-参数值 映射  结构： <DevNo,<OID,SnmpReqDTO></OID,SnmpReqDTO>
     */
    @Getter
    private static final Map<String, Map<String, SnmpRptDTO>> devSnmpParaMap = new ConcurrentHashMap<>(10);
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
    public static void initData(List<ParaInfo> paraList, ISysParamService sysParamService) {
        DevParaInfoContainer.sysParamService = sysParamService;
        Map<String, List<ParaInfo>> paraMapByDevType = paraList.stream().collect(Collectors.groupingBy(ParaInfo::getDevType));
        BaseInfoContainer.getDevNos().forEach(devNo -> {
            String devType = BaseInfoContainer.getDevInfoByNo(devNo).getDevType();
            devParaMap.put(devNo, assembleViewList(devNo, paraMapByDevType.get(devType)));
            ParaExtServiceFactory.genParaExtService(devType).setCacheDevParaViewInfo(devNo);
        });
        //SNMP 内存参数映射处理
        updateSnmpRptData();
//        //todo test
//        ParaViewInfo paraViewInfo1 = new ParaViewInfo();
//        paraViewInfo1.setDevType("0020012");
//        paraViewInfo1.setParaNo("1");
//        paraViewInfo1.setParaVal("1450.0000");
//        paraViewInfo1.setDevNo("19");
//        paraViewInfo1.setParaStrLen("8");
//        String linkKey1 = ParaHandlerUtil.genLinkKey(paraViewInfo1.getDevNo(), paraViewInfo1.getParaNo());
//
//        ParaViewInfo viewInfo1 = devParaMap.get(paraViewInfo1.getDevNo()).get(linkKey1);
//        BeanUtil.copyProperties(viewInfo1,paraViewInfo1,true);
//        devParaMap.get(paraViewInfo1.getDevNo()).put(linkKey1,paraViewInfo1);
//
//        ParaViewInfo paraViewInfo2 = new ParaViewInfo();
//        paraViewInfo2.setDevType("0020012");
//        paraViewInfo2.setParaNo("2");
//        paraViewInfo2.setParaVal("2.5");
//        paraViewInfo2.setDevNo("19");
//        paraViewInfo2.setParaStrLen("3");
//        String linkKey2 = ParaHandlerUtil.genLinkKey(paraViewInfo2.getDevNo(), paraViewInfo2.getParaNo());
//        ParaViewInfo viewInfo2 = devParaMap.get(paraViewInfo2.getDevNo()).get(linkKey2);
//        BeanUtil.copyProperties(viewInfo2,paraViewInfo2,true);
//        devParaMap.get(paraViewInfo2.getDevNo()).put(linkKey2,paraViewInfo2);
    }

    private static void updateSnmpRptData() {
        long t1 = System.currentTimeMillis();
        for (Map.Entry<String, Map<String, ParaViewInfo>> entry : devParaMap.entrySet()) {
            String currentDevNo = entry.getKey();
            Collection<ParaViewInfo> values = entry.getValue().values();
            for (ParaViewInfo paraView : values) {
                boolean canBeOpt = SysConfigConstant.IS_DEFAULT_TRUE.equals(paraView.getNdpaOutterStatus()) && !StringUtils.isEmpty(paraView.getRptOidSign());
                if (canBeOpt) {
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
        if (!devSnmpParaMap.containsKey(currentDevNo)) {
            devSnmpParaMap.put(currentDevNo, new ConcurrentHashMap<>(10));
        }
        Map<String, SnmpRptDTO> viewInfoMap = devSnmpParaMap.get(currentDevNo);
        String rptOid = SyntheticalUtil.genRptOid(value.getRptOidSign(), value.getParaCode(), sysParamService);
        SnmpRptDTO snmpRptDTO = new SnmpRptDTO();
        BeanUtils.copyProperties(value, snmpRptDTO);
        viewInfoMap.put(rptOid, snmpRptDTO);
    }

    /**
     * SNMP综合网管上报---OID参数后缀
     */
    public static final String SNMP_RPT_SUFFIX = ".1.1.1";

    /**
     * 初始化各个设备固定参数值 如：设备连接状态等
     */
    public static void initSnmpDevStatusRptData() {
        for (Map.Entry<String, Map<String, SnmpRptDTO>> snmpMap : devSnmpParaMap.entrySet()) {
            String currentDevNo = snmpMap.getKey();
            String oid0 = new ArrayList<>(snmpMap.getValue().entrySet()).get(0).getKey();
            String oidPrefix = oid0.substring(0, oid0.lastIndexOf(SNMP_RPT_SUFFIX));
            oidPrefix = oidPrefix.substring(0, oidPrefix.lastIndexOf("."));
            /**根据MIB库定义  设备连接状态在各个设备中：均为1.1.4*/
            String devOid4 = oidPrefix + ".4" + SNMP_RPT_SUFFIX;

            String oidDevNo = DevParaInfoContainer.getOidDevNo(devOid4);
            DevStatusInfo devStatusInfo = DevStatusContainer.getDevStatusInfo(oidDevNo);
            String isInterrupt = devStatusInfo.getIsInterrupt();
            String val = "0";
            if ("0".equals(isInterrupt)) {
                val = "1";
            }
            SnmpRptDTO rptDTO = SnmpRptDTO.builder()
                    .paraCode("4")
                    .paraName("设备连接状态")
                    .paraDatatype(INT)
                    .paraVal(val)
                    .build();

            devSnmpParaMap.get(currentDevNo).put(devOid4, rptDTO);
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
            devTypeParaList.sort(Comparator.comparing(paraInfo -> Integer.valueOf(paraInfo.getNdpaNo())));
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

    //生成每个参数对应的OID映射
    private static void genOidMap(String devNo, ParaInfo paraInfo) {
        if (paraInfo.getNdpaOutterStatus().equals(SysConfigConstant.IS_DEFAULT_TRUE) && !StringUtils.isEmpty(paraInfo.getNdpaRptOid())) {
            paraInfo.setDevNo(devNo);
            String oid = SyntheticalUtil.genRptOid(paraInfo.getNdpaRptOid(), paraInfo.getNdpaCode(), sysParamService);
            devParaOidMap.put(oid, paraInfo);
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
        viewInfo.setParaByteLen(paraInfo.getNdpaByteLen());
        viewInfo.setNdpaOutterStatus(paraInfo.getNdpaOutterStatus());
        viewInfo.setNdpaIsTopology(paraInfo.getNdpaIsTopology());
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
        return devParaMap.get(devNo).values().stream().filter(paraViewInfo -> paraViewInfo.getIsShow() == true).collect(Collectors.toList());
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

    /**
     * @param devNo  设备编号
     * @param paraNo 参数编号
     * @return 设备参数显示信息
     * @功能：修改指定参数是否显示
     */
    public static void setIsShow(String devNo, String paraNo, boolean result) {
        ParaViewInfo paraViewInfo = devParaMap.get(devNo).get(ParaHandlerUtil.genLinkKey(devNo, paraNo));
        if (ObjectUtil.isNotEmpty(paraViewInfo)) {
            paraViewInfo.setIsShow(result);
        }
    }

    /**
     * @param respData 协议解析响应数据
     * @return 数据是否发生变化
     * @功能：设置设备响应参数信息
     */
    public synchronized static boolean handlerRespDevPara(FrameRespData respData) {
        //ACU参数一直不停变化，需要特殊处理上报
        if (respData.getDevType().equals(SysConfigConstant.DEVICE_ACU)) {
            respNum++;
            if (respNum % 5 == 0) {
                respNum = 0;
            } else {
                return false;
            }
        }
        List<FrameParaData> frameParaList = respData.getFrameParaList();
        int num = 0;
        if (frameParaList != null && !frameParaList.isEmpty()) {
            for (FrameParaData frameParaData : frameParaList) {
                String devNo = frameParaData.getDevNo();
                String paraNo = frameParaData.getParaNo();
                ParaViewInfo paraViewInfo = devParaMap.get(devNo).get(ParaHandlerUtil.genLinkKey(devNo, paraNo));
                if (paraViewInfo != null && StringUtils.isNotEmpty(frameParaData.getParaVal()) && !frameParaData.getParaVal().equals(paraViewInfo.getParaVal())) {
                    paraViewInfo.setParaVal(frameParaData.getParaVal());
                    paraViewInfo.setParaOrigByte(frameParaData.getParaOrigByte());
                    //组合参数修改子参数值
                    if (paraViewInfo.getParaCmplexLevel().equals(PARA_COMPLEX_LEVEL_COMPOSE)) {
                        for (ParaViewInfo paraViewInfo1 : paraViewInfo.getSubParaList()) {
                            paraViewInfo1.setParaVal(frameParaList.stream().filter(frameParaData1 -> frameParaData1.getParaNo().equals(paraViewInfo1.getParaNo())).collect(Collectors.toList()).get(0).getParaVal());
                            paraViewInfo1.setParaOrigByte(frameParaList.stream().filter(frameParaData1 -> frameParaData1.getParaNo().equals(paraViewInfo1.getParaNo())).collect(Collectors.toList()).get(0).getParaOrigByte());
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
        updateSnmpRptData();
        return num > 0;
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
