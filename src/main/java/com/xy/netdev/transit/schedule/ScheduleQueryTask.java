package com.xy.netdev.transit.schedule;

import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.transit.IDevCmdSendService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 定时查询上报状态任务类
 *
 * @author duwenxu
 * @create 2021-03-10 18:07
 */
@Slf4j
public class ScheduleQueryTask implements Runnable {

    private final IDevCmdSendService devCmdSendService;
    private final List<FrameReqData> frameReqDataList;
    private final Long interval;
    private final Long commonInterval;

    public ScheduleQueryTask(List<FrameReqData> frameReqDataList, Long interval, Long commonInterval, IDevCmdSendService devCmdSendService) {
        this.frameReqDataList = frameReqDataList;
        this.interval = interval;
        this.commonInterval = commonInterval;
        this.devCmdSendService = devCmdSendService;
    }

    @Override
    public void run() {
        while (true) {
            try {
                //单个设备单次整体查询完之后的间隔
                Thread.sleep(commonInterval);
            } catch (Exception e) {
                log.error("线程+{}+休眠发生异常！", Thread.currentThread().getName());
            }
            frameReqDataList.forEach(data -> {
                String accessType = data.getAccessType();
                //分别调用接口或参数查询方法
                try {
                    if (SysConfigConstant.ACCESS_TYPE_PARAM.equals(accessType)) {
                        log.debug("baseInfo query:设备编号：{}--参数编号：{}--参数指令:{}", data.getDevNo(), data.getFrameParaList().get(0).getParaNo(), data.getCmdMark());
                        devCmdSendService.paraQuerySend(data.getDevNo(), data.getCmdMark());
                    } else if (SysConfigConstant.ACCESS_TYPE_INTERF.equals(accessType)) {
                        log.debug("baseInfo query:设备编号：{}-接口指令:{}", data.getDevNo(), data.getCmdMark());
                        devCmdSendService.interfaceQuerySend(data.getDevNo(), data.getCmdMark());
                    }
                } catch (Exception e) {
                    log.error("定时查询线程执行异常.当前查询设备编号：{}，当前查询类型：{}", data.getDevNo(), accessType,e);
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
}
