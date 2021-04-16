package com.xy.netdev.monitor.cronTask;

import com.xy.common.util.DateUtils;
import java.text.SimpleDateFormat;

/**
 * <p>
 * cron表达式工具类
 * </p>
 *
 * @author sunchao
 * @since 2021-04-16
 */
public class CronUtils {

    /**
     * 将毫秒值转化为cron所需格式
     * @param time 毫秒值
     * @return
     */
    public static String getCron(long time) {
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
