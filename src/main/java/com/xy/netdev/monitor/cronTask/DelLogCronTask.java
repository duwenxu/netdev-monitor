package com.xy.netdev.monitor.cronTask;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xy.common.exception.BaseException;
import com.xy.common.util.DateUtils;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.monitor.service.IAlertInfoService;
import com.xy.netdev.monitor.service.IOperLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>
 * 删除日志定时任务
 * </p>
 *
 * @author sunchao
 * @since 2021-04-16
 */
@EnableScheduling
@Component
@Slf4j
public class DelLogCronTask implements SchedulingConfigurer {

    @Autowired
    private ISysParamService sysParamService;
    @Autowired
    private IOperLogService operLogService;
    @Autowired
    private IAlertInfoService alertInfoService;

    /**
     * cron表达式
     * 过期时间的cron表达式
     */
    private String cron;

    /**
     * 定时任务执行间隔时间
     */
    private static int intervalTime;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        //获取间隔时间
        intervalTime  = Integer.parseInt(sysParamService.getParaRemark1(SysConfigConstant.DEV_LOG_SAVE_DAY));
        //启动定时任务执行一次：删除日志并计算新的cron表达式
        deleteLog();

        /**
         * 任务执行逻辑
         */
        Runnable task = new Runnable() {
            @Override
            public void run() {
                //删除日志并计算新的cron表达式
                deleteLog();
            }
        };

        /**
         * 定时任务执行周期修改
         */
        Trigger trigger = new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                CronTrigger trigger = new CronTrigger(cron);
                Date nextExec = trigger.nextExecutionTime(triggerContext);
                return nextExec;
            }
        };
        taskRegistrar.addTriggerTask(task,trigger);
    }

    @Transactional
    public void deleteLog(){
        try {
            //删除操作日志
            QueryWrapper queryWrapperOper = new QueryWrapper();
            String dateTimeOper = DateUtils.Millisecond2DateStr(System.currentTimeMillis()-intervalTime*24*60*60*1000);
            queryWrapperOper.le("LOG_TIME",dateTimeOper);
            operLogService.remove(queryWrapperOper);
            //删除告警日志
            QueryWrapper queryWrapperAlter = new QueryWrapper();
            String dateTimeAlter = DateUtils.Millisecond2DateStr(System.currentTimeMillis()-intervalTime*24*60*60*1000);
            queryWrapperAlter.le("ALERT_TIME",dateTimeAlter);
            alertInfoService.remove(queryWrapperAlter);
            //计算新的cron表达式
            long time = System.currentTimeMillis()+intervalTime*24*60*60*1000;
            cron = getCron(time);
            log.info("删除日志定时任务执行完成，下次执行时间："+DateUtils.Millisecond2DateStr(time));
        }catch (Exception e) {
            throw new BaseException("删除日志定时任务发生异常："+e.getMessage());
        }
    }

    /**
     * 将毫秒值转化为cron所需格式
     * @param time 毫秒值
     * @return
     */
    private String getCron(long time) {
        SimpleDateFormat sdfCron = new SimpleDateFormat("ss mm HH dd MM ?");
        String formatTimeStr = "";
        String date = DateUtils.Millisecond2DateStr(time);
        if (date != null) {
            try {
                formatTimeStr = sdfCron.format(DateUtils.getStdDate(date));
            } catch (Exception e) {
                throw e;
            }
        }
        return formatTimeStr;
    }
}
