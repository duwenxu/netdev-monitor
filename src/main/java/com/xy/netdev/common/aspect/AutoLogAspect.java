package com.xy.netdev.common.aspect;

import com.alibaba.fastjson.JSONObject;
import com.xy.common.util.DateUtils;
import com.xy.common.util.SpringContextUtils;
import com.xy.netdev.admin.api.ISysBaseAPI;
import com.xy.netdev.admin.entity.SysLog;
import com.xy.netdev.admin.entity.SysUser;
import com.xy.netdev.admin.service.ISysLogService;
import com.xy.netdev.common.annotation.AutoLog;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.IPUtils;
import com.xy.netdev.common.util.JwtUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 系统操作日志，切面处理类
 *
 * @Author luoqilong
 * @since 2019-09-26
 */
@Aspect
@Component
public class AutoLogAspect {

    @Autowired
    private ISysBaseAPI sysBaseAPI;
    @Autowired
    private ISysLogService sysLogService;
    @Pointcut("@annotation(com.xy.netdev.common.annotation.AutoLog)")
    public void logPointCut() {

    }
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        //执行方法
        Object result = point.proceed();
        //执行时长(毫秒)
        long time = System.currentTimeMillis() - beginTime;
        //保存日志
        saveSysLog(point, time);
        return result;
    }

    private void saveSysLog(ProceedingJoinPoint joinPoint, long time) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        SysLog sysLog = new SysLog();
        AutoLog syslog = method.getAnnotation(AutoLog.class);
        if(syslog != null){
            //注解上的描述,操作日志内容
            sysLog.setLogContent(syslog.value());
            sysLog.setLogType(syslog.logType());
        }
        //请求的方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        sysLog.setMethod(className + "." + methodName + "()");

        //设置操作类型
        if (sysLog.getLogType() == SysConfigConstant.LOG_TYPE_LOGOUT) {
            sysLog.setOperateType(getOperateType(methodName, syslog.operateType()));
        }

        //请求的参数
        Object[] args = joinPoint.getArgs();
        try{
            String params = JSONObject.toJSONString(args);
            sysLog.setRequestParam(params);
        }catch (Exception e){

        }

        //获取request
        HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
        //设置IP地址
        sysLog.setIp(IPUtils.getIpAddr(request));

        //获取登录用户信息
        String accessToken = request.getHeader("X-Access-Token");
        if(accessToken!=null){
            sysLog.setUserId(JwtUtil.getUserId(accessToken));
        }
        //耗时
        sysLog.setCostTime(time);
        sysLog.setCreateTime(DateUtils.now());
        //保存系统日志
        sysLogService.save(sysLog);
    }
    /**
     * 获取操作类型
     */
    private String getOperateType(String methodName,String operateType) {
        if (operateType.startsWith(SysConfigConstant.OPERATE_TYPE)) {
            return operateType;
        }
        if (methodName.startsWith("list")) {
            return SysConfigConstant.OPERATE_TYPE_QUERY;
        }
        if (methodName.startsWith("add")) {
            return SysConfigConstant.OPERATE_TYPE_ADD;
        }
        if (methodName.startsWith("edit")) {
            return SysConfigConstant.OPERATE_TYPE_UPDATE;
        }
        if (methodName.startsWith("delete")) {
            return SysConfigConstant.OPERATE_TYPE_DELETE;
        }
        if (methodName.startsWith("import")) {
            return SysConfigConstant.OPERATE_TYPE_5;
        }
        if (methodName.startsWith("export")) {
            return SysConfigConstant.OPERATE_TYPE_6;
        }
        return SysConfigConstant.OPERATE_TYPE_QUERY;
    }



}
