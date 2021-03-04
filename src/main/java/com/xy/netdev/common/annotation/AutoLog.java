package com.xy.netdev.common.annotation;


import com.xy.netdev.common.constant.SysConfigConstant;

import java.lang.annotation.*;

/**
 * 系统日志注解
 *
 * @author luoqilong
 * @since 2019-09-24
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoLog {

	/**
	 * 日志内容
	 * 
	 * @return
	 */
	String value() default "";

	/**
	 * 日志类型
	 * 
	 * @return 0:操作日志;1:登录日志;2:定时任务;
	 */
	String logType() default SysConfigConstant.LOG_TYPE_LOGOUT;
	
	/**
	 * 操作日志类型
	 * 
	 * @return （1查询，2添加，3修改，4删除）
	 */
    String operateType() default "0";
}
