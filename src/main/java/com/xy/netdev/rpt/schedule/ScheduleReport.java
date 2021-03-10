package com.xy.netdev.rpt.schedule;

import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.monitor.constant.MonitorConstants;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.service.IBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 设备状态上报类
 *
 * @author duwenxu
 * @create 2021-03-10 11:39
 */
@Slf4j
//@Component
public class ScheduleReport {

    @Autowired
    private IBaseInfoService baseInfoService;

    public void baseInfoReport() {
        List<BaseInfo> baseInfos = baseInfoService.list().stream().filter(base -> base.getDevStatus().equals(SysConfigConstant.DEV_STATUS_NEW)).collect(Collectors.toList());
        ConcurrentHashMap<String, List<FrameParaInfo>> baseParaInfoMap = new ConcurrentHashMap<>(10);
        ConcurrentHashMap<String, List<Interface>> baseInterInfoMap = new ConcurrentHashMap<>(10);
        baseInfos.forEach(base -> {
            //获取所有可读参数
            List<FrameParaInfo> parasByDevType = BaseInfoContainer.getParasByDevType(base.getDevType());
            List<FrameParaInfo> readParasByDevType = parasByDevType.stream().parallel()
                    .filter(param -> MonitorConstants.READ_ONLY.equals(param.getNdpaAccessRight()) || MonitorConstants.READ_WRITE.equals(param.getNdpaAccessRight())).collect(Collectors.toList());
            baseParaInfoMap.put(base.getDevNo(), readParasByDevType);

            //获取所有查询接口
            List<Interface> interfacesByDevType = BaseInfoContainer.getInterfacesByDevType(base.getDevType());
            List<Interface> queryIntersByDevType = interfacesByDevType.stream().filter(inter -> MonitorConstants.QUERY.equals(inter.getItfType())).collect(Collectors.toList());
            baseInterInfoMap.put(base.getDevNo(), queryIntersByDevType);
        });


        baseParaInfoMap.entrySet().forEach(entry->{

        });
    }


}
