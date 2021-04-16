package com.xy.netdev.monitor.cronTask;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xy.common.exception.BaseException;
import com.xy.common.util.DateUtils;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
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
public class DelOperCronTask implements SchedulingConfigurer {

    @Autowired
    private ISysParamService sysParamService;
    @Autowired
    private IOperLogService operLogService;

    /**
     * cron表达式
     * 过期时间的cron表达式
     */
    private String cron;

    /**
     * 操作日志的保留时间
     */
    private static int logIntervalTime;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        //操作日志保留天数
        logIntervalTime  = Integer.parseInt(sysParamService.getParaRemark1(SysConfigConstant.DEV_LOG_SAVE_DAY));
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
            String dateTimeOper = DateUtils.Millisecond2DateStr(System.currentTimeMillis()-logIntervalTime*24*60*60*1000);
            queryWrapperOper.le("LOG_TIME",dateTimeOper);
            operLogService.remove(queryWrapperOper);
            //计算新的cron表达式
            long time = System.currentTimeMillis()+logIntervalTime*24*60*60*1000;
            cron = CronUtils.getCron(time);
            log.info("删除操作日志定时任务执行完成，下次执行时间："+DateUtils.Millisecond2DateStr(time));
        }catch (Exception e) {
            throw new BaseException("删除操作日志定时任务发生异常："+e.getMessage());
        }
    }
}
