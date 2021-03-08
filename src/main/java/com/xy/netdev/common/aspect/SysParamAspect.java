package com.xy.netdev.common.aspect;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xy.common.annotation.Param;
import com.xy.common.annotation.User;
import com.xy.common.model.Result;
import com.xy.common.util.ConvertUtils;
import com.xy.netdev.admin.entity.SysUser;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.admin.service.ISysUserService;
import com.xy.netdev.common.constant.SysConfigConstant;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 参数aop类
 * @author luoqilong
 * @since 2019-09-26
 */
@Slf4j
@Aspect
@Component
public class SysParamAspect {
    @Autowired
    private ISysParamService paramService;
    @Autowired
    private ISysUserService userService;
    @Pointcut("execution(public * com.xy.netdev..*.*Controller.*(..))")
    public void excudeService() {
    }

    @Around("excudeService()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
    	long time1=System.currentTimeMillis();	
        Object result = pjp.proceed();
        long time2=System.currentTimeMillis();
        log.debug("获取JSON数据 耗时："+(time2-time1)+"ms");
        long start=System.currentTimeMillis();
        this.parseDictText(result);
        long end=System.currentTimeMillis();
        log.debug("解析注入JSON数据  耗时"+(end-start)+"ms");
        return result;
    }

    /**
     * @param result
     */
    private void parseDictText(Object result) {
        if (result instanceof Result) {
            if (((Result) result).getResult() instanceof IPage) {
                List<JSONObject> items = new ArrayList<>();
                for (Object record : ((IPage) ((Result) result).getResult()).getRecords()) {
                    recordsHandler(record,items);
                }
                ((IPage) ((Result) result).getResult()).setRecords(items);
            }
            if (((Result) result).getResult() instanceof List){
                List<JSONObject> items = new ArrayList<>();
                for (Object record : (List) ((Result) result).getResult()){
                    if(record != null){
                        recordsHandler(record,items);
                    }
                }
                ((Result) result).setResult(items);
            }
        }
    }

    /**
     * 遍历查询结果，处理需转参数字段
     * @param record
     * @param items
     */
    private void recordsHandler(Object record,List<JSONObject> items){
        ObjectMapper mapper = new ObjectMapper();
        String json="{}";
        try {
            json = mapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            log.error("json解析失败"+e.getMessage(),e);
        }
        JSONObject item = JSONObject.parseObject(json);
        for (Field field : ConvertUtils.getAllFields(record)) {
            if (field.getAnnotation(Param.class) != null) {
                String paraName = "";
                String key = String.valueOf(item.get(field.getName()));
                //翻译参数值为对应的name
                if(ConvertUtils.isNotEmpty(key)){
                    paraName = translateParamValue(key);
                }else{
                    //如果数据库返回为空，则判断注解上是否有默认值
                    String paraCode = field.getAnnotation(Param.class).paraCode();
                    if(ConvertUtils.isNotEmpty(paraCode)){
                        paraName = translateParamValue(paraCode);
                    }else{
                        paraName = field.getAnnotation(Param.class).paraName();
                    }
                }
                item.put(field.getName() + SysConfigConstant.PARA_NAME_SUFFIX, paraName);
            }
            if(field.getAnnotation(User.class) != null){
                String key = String.valueOf(item.get(field.getName()));
                String userName = translateUserName(key);
                item.put(field.getName() + SysConfigConstant.USER_NAME_SUFFIX, userName);
            }
        }
        items.add(item);
    }

    /**
     *  翻译参数名称
     * @param key
     * @return
     */
    private String translateParamValue(String key) {
    	if(ConvertUtils.isEmpty(key)) {
    		return null;
    	}
        StringBuffer textValue=new StringBuffer();
        String[] keys = key.split(",");
        for (String k : keys) {
            String tmpValue = null;
            if (k.trim().length() == 0) {
                continue; //跳过循环
            }
            tmpValue = paramService.getParaName(k.trim());
            if (tmpValue != null) {
                if (!"".equals(textValue.toString())) {
                    textValue.append(",");
                }
                textValue.append(tmpValue);
            }
        }
        return textValue.toString();
    }

    private String translateUserName(String key){
        if(ConvertUtils.isEmpty(key)) {
            return null;
        }
        String userName = "";
        SysUser user = userService.getById(key);
        if(user != null){
            userName = user.getUserName();
        }
        return userName;
    }
}
