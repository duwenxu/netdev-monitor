package com.xy.netdev.rpt.schedule;

import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
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
public class ScheduleReportTask implements Runnable{

    private final List<ScheduleReqBody> scheduleReqBodyList;
    private final Long inteval;

    public ScheduleReportTask(List<ScheduleReqBody> scheduleReqBodyList, Long inteval) {
        this.scheduleReqBodyList = scheduleReqBodyList;
        this.inteval = inteval;
    }

    @Override
    public void run() {
        try {
            Thread.sleep((long)10*1000);
        } catch (Exception e) {
            log.error("线程+{}+休眠发生异常！",Thread.currentThread().getName());
        }
        //todo 每一次所有查询完毕之后的间隔
        scheduleReqBodyList.forEach(body->{
            IParaPrtclAnalysisService analysisService = body.getPrtclAnalysisService();
            FrameReqData frameReqData = body.getFrameReqData();
            analysisService.queryPara(frameReqData);
            //根据不同设备指定间隔查询
            try {
                Thread.sleep(inteval);
            } catch (InterruptedException e) {
                log.error("线程+{}+休眠发生异常！",Thread.currentThread().getName());
            }
        });
    }
}
