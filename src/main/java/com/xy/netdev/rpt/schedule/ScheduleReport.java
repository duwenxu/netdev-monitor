package com.xy.netdev.rpt.schedule;

import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.factory.ParaPrtclFactory;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.constant.MonitorConstants;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.monitor.service.IBaseInfoService;
import com.xy.netdev.rpt.bo.ScheduleReqBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 设备状态上报查询发起类
 *
 * @author duwenxu
 * @create 2021-03-10 11:39
 */
@Slf4j
@Component
public class ScheduleReport {

    @Autowired
    private IBaseInfoService baseInfoService;

    public void baseInfoWrapper() {
        List<BaseInfo> baseInfos = baseInfoService.list().stream().filter(base -> base.getDevStatus().equals(SysConfigConstant.DEV_STATUS_NEW)).collect(Collectors.toList());
        //某设备需要查询的信息 包括参数和接口
        ConcurrentHashMap<BaseInfo, List<?>> baseQueryMap = new ConcurrentHashMap<>(10);
        baseInfos.forEach(base -> {
            //获取所有可读参数
            List<FrameParaInfo> parasByDevType = BaseInfoContainer.getParasByDevType(base.getDevType());
            List<FrameParaInfo> readParasByDevType = parasByDevType.stream().parallel()
                    .filter(param -> MonitorConstants.READ_ONLY.equals(param.getNdpaAccessRight()) || MonitorConstants.READ_WRITE.equals(param.getNdpaAccessRight())).collect(Collectors.toList());
            ArrayList<Object> queryItems = new ArrayList<>(readParasByDevType);

            //获取所有查询接口
            List<Interface> interfacesByDevType = BaseInfoContainer.getInterfacesByDevType(base.getDevType());
            List<Interface> queryIntersByDevType = interfacesByDevType.stream().filter(inter -> MonitorConstants.QUERY.equals(inter.getItfType())).collect(Collectors.toList());
            queryItems.addAll(queryIntersByDevType);
            baseQueryMap.put(base, queryItems);
        });

        //单个设备所有查询对象的封装list映射
        Map<BaseInfo,List<ScheduleReqBody>> scheduleReqBodyMap = new ConcurrentHashMap<>(20);
        List<ScheduleReqBody> scheduleReqBodyList = new ArrayList<>();
        baseQueryMap.forEach((base, queryItems) -> {
            queryItems.forEach(item -> {
                IParaPrtclAnalysisService handler = null;
                String cmdMark = null;
                List<FrameParaData> frameParaList= new ArrayList<>();
                if (item instanceof FrameParaInfo) {
                    //获取参数对应的格式协议及拼装查询类请求参数
                    FrameParaInfo item1 = (FrameParaInfo) item;
                    PrtclFormat prtclFormat = item1.getInterfacePrtcl();
                    handler = ParaPrtclFactory.genHandler(prtclFormat.getFmtHandlerClass());
                    cmdMark = item1.getCmdMark();
                    FrameParaData paraData = FrameParaData.builder()
                            .devNo(base.getDevNo())
                            .devType(base.getDevType())
                            .paraNo(item1.getParaNo())
                            .paraVal(item1.getParaVal())
                            .build();
                    frameParaList.add(paraData);
                } else if (item instanceof Interface) {
                    //获取接口对应的格式协议及处理类
                    String itfCode = ((Interface) item).getItfCode();
                    PrtclFormat prtclFormat = BaseInfoContainer.getInterLinkFmtFormat(base.getDevType(), itfCode);
                    handler = ParaPrtclFactory.genHandler(prtclFormat.getFmtHandlerClass());
                    cmdMark = ((Interface) item).getItfCmdMark();
                }

                //组装定时查询结构类
                ScheduleReqBody scheduleReqBody = ScheduleReqBody.builder()
                        .prtclAnalysisService(handler)
                        .frameReqData(
                                FrameReqData.builder()
                                        .devType(base.getDevType())
                                        .devNo(base.getDevNo())
                                        .cmdMark(cmdMark)
                                        .frameParaList(frameParaList)
                                        .build()
                        ).build();
                scheduleReqBodyList.add(scheduleReqBody);
            });
            scheduleReqBodyMap.put(base, scheduleReqBodyList);
        });

        execReportTask(scheduleReqBodyMap);
    }

    public static void execReportTask(Map<BaseInfo,List<ScheduleReqBody>> scheduleReqBodyMap){
        scheduleReqBodyMap.forEach((base,queryList)->{
            Integer devIntervalTime = base.getDevIntervalTime();
            long interval = Long.parseLong(base.getDevIntervalTime() + "");
            ScheduleReportTask scheduleReportTask = new ScheduleReportTask(queryList,interval);
            Thread thread = new Thread(scheduleReportTask,base.getDevName()+"-reportQuery-thread");
            thread.start();
        });
    }

}
