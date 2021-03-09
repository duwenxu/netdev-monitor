package com.xy.netdev.common.util;
import java.text.SimpleDateFormat;

/**
  *    日期格式化工具类,使用ThreadLocal解决SimpleDateFormat非线程安全问题
  */
public class DateThreadLocal {
    private static ThreadLocal<SimpleDateFormat> t1 = new ThreadLocal<>();

    public static SimpleDateFormat getSimpleDateFormat(String datePattern) {
        SimpleDateFormat sdf = null;
        sdf = t1.get();
        if(sdf == null) {
            sdf = new SimpleDateFormat(datePattern);
            t1.set(sdf);
        }
        return sdf;
    }

    /**
     * 清除	 ThreadLocal中保存的变量,不然ThreadLocal不会被垃圾回收期回收,会造成内存泄漏
     */
    public static void clear() {
        t1.remove();
    }
}
