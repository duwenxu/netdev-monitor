package com.xy.netdev.common.util;

import com.xy.common.exception.BaseException;

import java.util.Date;

/**
  *    日期格式化工具类,使用ThreadLocal解决SimpleDateFormat非线程安全问题
  */
public class DateTools {
    public static final String FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_YMD = "yyyy-MM-dd";
    public static final String FORMAT_YMDHMS = "yyyyMMddHHmmss";

    public static String getDateTime() {
        Date currentTime = new Date();
        return DateThreadLocal.getSimpleDateFormat(FORMAT).format(currentTime);
    }

    public static String getStdDateByDateTime(String dateTime) {
        try {
            return DateThreadLocal.getSimpleDateFormat(FORMAT_YMD).format(DateThreadLocal.getSimpleDateFormat(FORMAT).parse(dateTime));
        }catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    public static String getStdDate() {
        Date currentTime = new Date();
        return DateThreadLocal.getSimpleDateFormat(FORMAT_YMD).format(currentTime);
    }
}
