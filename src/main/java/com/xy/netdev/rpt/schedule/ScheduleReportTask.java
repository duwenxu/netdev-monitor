package com.xy.netdev.rpt.schedule;

import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.transit.IDataSendService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.List;

/**
 * 定时查询上报状态任务类
 *
 * @author duwenxu
 * @create 2021-03-10 18:07
 */
@Slf4j
public class ScheduleReportTask implements Runnable {

    @Resource
    private IDataSendService dataSendService;

    private final List<FrameReqData> frameReqDataList;
    private final Long interval;
    private final Long commonInterval;

    public ScheduleReportTask(List<FrameReqData> frameReqDataList, Long interval, Long commonInterval) {
        this.frameReqDataList = frameReqDataList;
        this.interval = interval;
        this.commonInterval = commonInterval;
    }

    @Override
    public void run() {
        try {
            //单个设备单次整体查询完之后的间隔
            Thread.sleep(commonInterval);
        } catch (Exception e) {
            log.error("线程+{}+休眠发生异常！", Thread.currentThread().getName());
        }
        frameReqDataList.forEach(data -> {
            String accessType = data.getAccessType();
            //分别调用接口或参数查询方法
            if (SysConfigConstant.ACCESS_TYPE_PARAM.equals(accessType)) {
                dataSendService.paraQuerySend(data);
            } else if (SysConfigConstant.ACCESS_TYPE_INTERF.equals(accessType)) {
                dataSendService.interfaceQuerySend(data);
            }
            //根据不同设备指定间隔查询
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                log.error("线程+{}+休眠发生异常！", Thread.currentThread().getName());
            }
        });
    }
}
