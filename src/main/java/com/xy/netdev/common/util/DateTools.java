package com.xy.netdev.common.util;

import com.xy.common.exception.BaseException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public static boolean isValidDate(String str) {
        boolean convertSuccess=true;
        //指定日期格式为四位年/两位月份/两位日期，注意yyyy/MM/dd区分大小写；
        SimpleDateFormat format = new SimpleDateFormat(FORMAT);
        try {
        // 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
            format.setLenient(false);
            format.parse(str);
        } catch (ParseException e) {
            // e.printStackTrace();
            // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
            convertSuccess=false;
        }
        return convertSuccess;
    }
}
