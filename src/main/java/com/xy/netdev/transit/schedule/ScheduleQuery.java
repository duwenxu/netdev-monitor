package com.xy.netdev.transit.schedule;

import com.xy.common.util.DateUtils;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseContainerLoader;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevAlertInfoContainer;
import com.xy.netdev.container.DevLogInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.constant.MonitorConstants;
import com.xy.netdev.monitor.entity.AlertInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.transit.IDevCmdSendService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.xy.netdev.monitor.constant.MonitorConstants.*;

/**
 * 设备状态定时查询发起类
 *
 * @author duwenxu
 * @create 2021-03-10 11:39
 */
@Slf4j
@Component
public class ScheduleQuery  implements ApplicationRunner{

    @Autowired
    private IDevCmdSendService devCmdSendService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("-----设备状态定时查询开始...");
        try {
//            doScheduleQuery();
            DevLogInfoContainer.initTestStatus();
        } catch (Exception e) {
            log.error("设备状态定时查询异常...", e);
        }
    }

    /**
     * 设备参数定时查询
     */
    public void doScheduleQuery() {
        List<BaseInfo> baseInfos = ScheduleQueryHelper.getAvailableBases().stream().filter(base -> base.getDevType().equals("0020008")).collect(Collectors.toList());
//        List<BaseInfo> baseInfos = ScheduleQueryHelper.getAvailableBases();
        //单个设备所有查询对象的封装list映射
        Map<BaseInfo, List<FrameReqData>> scheduleReqBodyMap = new ConcurrentHashMap<>(20);
        baseInfos.forEach(base -> {
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
                    log.warn("设备编号：{}--参数编号：{}的cmdMark为空", base.getDevNo(), frameParaInfo.getParaNo());
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
//                scheduleReqBodyList.add(frameReqData);
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
                    log.warn("接口编号：{}的cmdMark为空", item.getItfId());
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
        //执行查询任务
        execQueryTask(scheduleReqBodyMap);
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
//            thread.setDaemon(true);
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
}
