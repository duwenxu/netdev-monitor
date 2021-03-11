package com.xy.netdev.rpt.schedule;

import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.rpt.bo.ScheduleReqBody;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 定时查询上报状态任务类
 *
 * @author duwenxu
 * @create 2021-03-10 18:07
 */
@Slf4j
public class ScheduleReportTask implements Runnable {

    private final List<ScheduleReqBody> scheduleReqBodyList;
    private final Long interval;
    private final Long commonInterval;

    public ScheduleReportTask(List<ScheduleReqBody> scheduleReqBodyList, Long interval, Long commonInterval) {
        this.scheduleReqBodyList = scheduleReqBodyList;
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
        scheduleReqBodyList.forEach(body -> {
            Object analysisService = body.getHandlerClass();
            FrameReqData frameReqData = body.getFrameReqData();
            if (analysisService instanceof IParaPrtclAnalysisService) {
                ((IParaPrtclAnalysisService) analysisService).queryPara(frameReqData);
            } else if (analysisService instanceof IQueryInterPrtclAnalysisService) {
                ((IQueryInterPrtclAnalysisService) analysisService).queryPara(frameReqData);
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
