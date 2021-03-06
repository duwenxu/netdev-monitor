package com.xy.netdev.transit.schedule;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.container.DevStatusContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.service.snmp.SnmpReqDTO;
import com.xy.netdev.frame.service.snmp.SnmpTransceiverServiceImpl;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.bo.ParaViewInfo;
import com.xy.netdev.monitor.constant.MonitorConstants;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.rpt.service.IDevStatusReportService;
import com.xy.netdev.synthetical.util.SyntheticalUtil;
import com.xy.netdev.transit.IDevCmdSendService;
import com.xy.netdev.transit.ISnmpDataReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.xy.netdev.common.constant.SysConfigConstant.PARA_COMPLEX_LEVEL_SUB;
import static com.xy.netdev.common.constant.SysConfigConstant.SNMP;
import static com.xy.netdev.monitor.constant.MonitorConstants.*;

/**
 * 设备状态定时查询发起类
 *
 * @author duwenxu
 * @create 2021-03-10 11:39
 */
@Slf4j
@Order(200)
@Component
public class ScheduleQuery  implements ApplicationRunner{

    @Autowired
    private IDevCmdSendService devCmdSendService;
    @Autowired
    private ISnmpDataReceiveService snmpDataReceiveService;
    @Autowired
    private SnmpTransceiverServiceImpl snmpTransceiverService;
    @Autowired
    private IDevStatusReportService devStatusReportService;
    private static final String PING_THREAD_NAME ="basePingThread";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("-----设备状态定时查询开始...");
        try {
           doScheduleQuery();
        } catch (Exception e) {
            log.error("设备状态定时查询异常...", e);
        }
    }

    /**
     * 设备参数定时查询
     */
    public void doScheduleQuery() {
//        List<BaseInfo> queryBaseInfo = ScheduleQueryHelper.getAvailableBases().stream().filter(base -> base.getDevType().equals("0020040")||base.getDevType().equals("0020014")||base.getDevType().equals("0020015")||base.getDevType().equals("0020024")||base.getDevType().equals("0020025")||base.getDevType().equals("0020027")||base.getDevType().equals("0020044")||base.getDevType().equals("0020045")||base.getDevType().equals("0020046")||base.getDevType().equals("0020047")).collect(Collectors.toList());
        List<BaseInfo> queryBaseInfo = ScheduleQueryHelper.getAvailableBases().stream().filter(base -> base.getDevType().equals("0020041") || base.getDevType().equals("0020042")).collect(Collectors.toList());
        //一类车
//        List<BaseInfo> queryBaseInfo = ScheduleQueryHelper.getAvailableBases().stream().filter(base ->base.getDevType().equals("0020003") || base.getDevType().equals("0020004") || base.getDevType().equals("0020005") || base.getDevType().equals("0020006") || base.getDevType().equals("0020007") || base.getDevType().equals("0020008") || base.getDevType().equals("0020021")).collect(Collectors.toList());
        //三类车
        //List<BaseInfo> queryBaseInfo = ScheduleQueryHelper.getAvailableBases().stream().filter(base -> base.getDevType().equals("0020001")).collect(Collectors.toList());
        List<BaseInfo> pingBaseInfo = ScheduleQueryHelper.getAvailableBases();
        //单个设备所有查询对象的封装list映射
        Map<BaseInfo, List<FrameReqData>> scheduleReqBodyMap = new ConcurrentHashMap<>(20);
        //单个设备所有查询对象的封装list映射---SNMP
        Map<BaseInfo, List<FrameReqData>> scheduleSnmpReqBodyMap = new ConcurrentHashMap<>(20);
        queryBaseInfo.forEach(base -> {
            //获取所有可读参数
            List<FrameParaInfo> parasByDevType = BaseInfoContainer.getParasByDevType(base.getDevType());
            List<FrameParaInfo> readParasByDevType = parasByDevType.stream().parallel()
                    .filter(param -> MonitorConstants.READ_ONLY.equals(param.getNdpaAccessRight()) || MonitorConstants.READ_WRITE.equals(param.getNdpaAccessRight())).collect(Collectors.toList());
            List<FrameReqData> scheduleReqBodyList = new ArrayList<>();
            //参数查询对象封装
            for (FrameParaInfo frameParaInfo : readParasByDevType) {
                List<FrameParaData> frameParaList = new ArrayList<>();
                //获取参数对应的格式协议及拼装查询类请求参数
                PrtclFormat prtclFormat = frameParaInfo.getInterfacePrtcl();
                if (prtclFormat == null) {
                    continue;
                }
                String cmdMark = frameParaInfo.getCmdMark();
                if (StringUtils.isEmpty(cmdMark)) {
                    log.warn("设备编号：[{}]--参数编号：[{}]的cmdMark为空", base.getDevNo(), frameParaInfo.getParaNo());
                    continue;
                }
                FrameParaData paraData = FrameParaData.builder()
                        .devNo(base.getDevNo())
                        .devType(base.getDevType())
                        .paraNo(frameParaInfo.getParaNo())
                        .paraVal(frameParaInfo.getParaVal())
                        .build();
                frameParaList.add(paraData);
                FrameReqData frameReqData = frameReqDataWrapper(base, cmdMark, frameParaList);
                frameReqData.setAccessType(SysConfigConstant.ACCESS_TYPE_PARAM);
                scheduleReqBodyList.add(frameReqData);
            }

            //获取所有查询接口
            List<String> interTypes = Arrays.asList(SINGLE_QUERY, PAGE_QUERY, PACKAGE_QUERY);
            List<Interface> interfacesByDevType = BaseInfoContainer.getInterfacesByDevType(base.getDevType());
            List<Interface> queryIntersByDevType = interfacesByDevType.stream()
                    .filter(inter -> interTypes.contains(inter.getItfType())).collect(Collectors.toList());
            //接口查询对象封装
            for (Interface item : queryIntersByDevType) {
                //获取接口对应的格式协议及处理类
                String cmdMark = item.getItfCmdMark();
                if (StringUtils.isEmpty(cmdMark)) {
                    log.warn("设备编号：[{}]--接口编号：[{}]的cmdMark为空",base.getDevNo(), item.getItfId());
                    continue;
                }
                PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterface(base.getDevType(), cmdMark);
                if (prtclFormat == null) {
                    continue;
                }
                FrameReqData frameReqData = frameReqDataWrapper(base, cmdMark, new ArrayList<>());
                frameReqData.setAccessType(SysConfigConstant.ACCESS_TYPE_INTERF);
                scheduleReqBodyList.add(frameReqData);
            }
            scheduleReqBodyMap.put(base, scheduleReqBodyList);
        });

        //SNMP设备参数和接口
        for (Map.Entry<BaseInfo, List<FrameReqData>> entry : scheduleReqBodyMap.entrySet()) {
            String devNetPtcl = entry.getKey().getDevNetPtcl();
            if (SNMP.equals(devNetPtcl)){
                scheduleSnmpReqBodyMap.put(entry.getKey(),entry.getValue());
                scheduleReqBodyMap.remove(entry.getKey());
            }
        }

        //执行查询任务
        execQueryTask(scheduleReqBodyMap);
        execSnmpTask(scheduleSnmpReqBodyMap);
        execBasePing(pingBaseInfo);
    }

    /**
     * 设备通信检测
     * @param baseInfos 设备信息
     */
    private void execBasePing(List<BaseInfo> baseInfos) {
        ThreadUtil.newSingleExecutor().submit(()->{
            Thread.currentThread().setName(PING_THREAD_NAME);
            while (true){
                Thread.sleep(5000);
                baseInfos.forEach(baseInfo->{
                    //默认超时时间 200
                    boolean ping = NetUtil.ping(baseInfo.getDevIpAddr());
                    String devNo = baseInfo.getDevNo();
                    log.debug("设备：[{}]Ping地址：[{}]成功：{}", baseInfo.getDevName(),baseInfo.getDevIpAddr(),ping);
                    String isActive = ping ? "0" : "1";
                    boolean isChanged = DevStatusContainer.setInterrupt(devNo, isActive);
                    if(isChanged){
                        devStatusReportService.rptInterrupted(devNo,isActive);
                        /**同时修改SNMP数据中的设备连接状态*/
                        updateSnmpRptStatus(ping, devNo);
                    }
                });
            }
        });
    }

    private void updateSnmpRptStatus(boolean ping, String devNo) {
        Map<String, Set<String>> statusOidMap = DevParaInfoContainer.getDevNoStatusOidMap();
        if (!statusOidMap.containsKey(devNo)){
            statusOidMap.put(devNo,new HashSet<>(5));
        }
        Set<String> oid4= statusOidMap.get(devNo);
        for (String oid : oid4) {
            if (StringUtils.isNotBlank(oid)){
                /**上报信息中 0：中断 1：正常  与设备状态中的状态相反*/
                String rptActive = ping ? "1" : "0";
                DevParaInfoContainer.getDevSnmpParaMap().get(devNo).get(oid).setParaVal(rptActive);
            }
        }
    }

    /**
     * 查询参数执行
     *
     * @param scheduleReqBodyMap 参数信息
     */
    public void execQueryTask(Map<BaseInfo, List<FrameReqData>> scheduleReqBodyMap) {
        Long commonInterval = ScheduleQueryHelper.getQueryInterval();
        scheduleReqBodyMap.forEach((base, queryList) -> {
            long interval = Long.parseLong(base.getDevIntervalTime() + "");
            ScheduleQueryTask scheduleReportTask = new ScheduleQueryTask(queryList, interval, commonInterval, devCmdSendService);
            Thread thread = new Thread(scheduleReportTask, base.getDevName() + "-scheduleQuery-thread");
            thread.start();
        });
    }

    /**
     * 定时查询请求参数封装
     *
     * @return ScheduleReqBody
     */
    private static FrameReqData frameReqDataWrapper(BaseInfo base, String cmdMark, List<FrameParaData> frameParaList) {
        //组装定时查询结构类
        return FrameReqData.builder()
                .devType(base.getDevType())
                .devNo(base.getDevNo())
                .cmdMark(cmdMark)
                .frameParaList(frameParaList)
                .build();
    }

    private void execSnmpTask(Map<BaseInfo, List<FrameReqData>> scheduleSnmpReqBodyMap) {
        Map<BaseInfo, List<SnmpReqDTO>> snmpBaseReqMap = snmpConvert(scheduleSnmpReqBodyMap);
        Long commonInterval = ScheduleQueryHelper.getQueryInterval();
        snmpBaseReqMap.forEach((snmpBase, queryList) -> {
            long interval = Long.parseLong(snmpBase.getDevIntervalTime() + "");
            SnmpScheduleQueryTask scheduleReportTask = new SnmpScheduleQueryTask(queryList, interval, commonInterval, snmpDataReceiveService,snmpTransceiverService,snmpBase.getDevIpAddr());
            Thread thread = new Thread(scheduleReportTask, snmpBase.getDevName() + "-snmpScheduleQuery-thread");
            thread.start();
        });
    }

    /**
     * 将普通设备的查询对象转换为SNMP的查询对象
     * 子参数将会在其对应的父参数一起接口查询，子参数不单独查询
     * @param scheduleSnmpReqBodyMap 普通设备的查询结构体
     * @return SNMP设备的查询对象
     */
    private Map<BaseInfo, List<SnmpReqDTO>> snmpConvert(Map<BaseInfo, List<FrameReqData>> scheduleSnmpReqBodyMap) {
        ConcurrentHashMap<BaseInfo, List<SnmpReqDTO>> snmpBaseMap = new ConcurrentHashMap<>();
        for (Map.Entry<BaseInfo, List<FrameReqData>> entry : scheduleSnmpReqBodyMap.entrySet()) {
            List<SnmpReqDTO> snmpList = new CopyOnWriteArrayList<>();
            for (FrameReqData frameReqData : entry.getValue()) {
                SnmpReqDTO snmpReqDTO = frameReq2SnmpReq(frameReqData);
                if (snmpReqDTO!=null){
                    snmpList.add(snmpReqDTO);
                }
            }
            snmpBaseMap.put(entry.getKey(),snmpList);
        }
        return snmpBaseMap;
    }

    public SnmpReqDTO frameReq2SnmpReq(FrameReqData frameReqData) {
        List<FrameParaData> frameParaList = frameReqData.getFrameParaList();
        for (FrameParaData frameParaData : frameParaList) {
            FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByNo(frameParaData.getDevType(), frameParaData.getParaNo());
            String cmdMark = paraInfo.getCmdMark();
            frameParaData.setParaCmk(cmdMark);
            frameParaData.setOid(snmpTransceiverService.oidSplic(cmdMark,frameParaData.getDevType()));
        }
        SnmpReqDTO snmpReqDTO = SnmpReqDTO.builder()
                .accessType(frameReqData.getAccessType())
                .cmdMark(frameReqData.getCmdMark())
                .devNo(frameReqData.getDevNo())
                .devType(frameReqData.getDevType())
                .operType(frameReqData.getOperType())
                .frameParaList(frameParaList)
                .build();
        BeanUtil.copyProperties(frameReqData,snmpReqDTO,true);
        return snmpReqDTO;
    }


}
