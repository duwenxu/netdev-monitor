package com.xy.netdev.admin.service.impl;

import com.xy.common.util.ConvertUtils;
import com.xy.common.util.DateUtils;
import com.xy.common.util.SpringContextUtils;
import com.xy.netdev.admin.api.ISysBaseAPI;
import com.xy.netdev.admin.entity.SysLog;
import com.xy.netdev.admin.entity.SysParam;
import com.xy.netdev.admin.entity.SysUser;
import com.xy.netdev.admin.mapper.SysLogMapper;
import com.xy.netdev.admin.mapper.SysUserMapper;
import com.xy.netdev.admin.mapper.SysUserRoleMapper;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.IPUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * 底层共通业务API，提供其他独立模块调用
 * @author luoqilong
 * @since 2019-09-17
 */
@Slf4j
@Service
public class SysBaseApiImpl implements ISysBaseAPI {
	public static final String DB_TYPE_MYSQL="MYSQL";
	public static String DB_TYPE = "";
	public static SysUser LoginUser;
	@Resource
	private SysLogMapper sysLogMapper;
	@Resource
	private SysUserMapper userMapper;
	@Resource
	private SysUserRoleMapper sysUserRoleMapper;
	@Autowired
	private ISysParamService sysParamService;

	@Override
	public void addLog(String LogContent, String logType, String operatetype) {
		SysLog sysLog = new SysLog();
		//注解上的描述,操作日志内容
		sysLog.setLogContent(LogContent);
		sysLog.setLogType(logType);
		sysLog.setOperateType(operatetype);
		//请求的方法名
		//请求的参数
		try {
			//获取request
			HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
			//设置IP地址
			sysLog.setIp(IPUtils.getIpAddr(request));
		} catch (Exception e) {
			sysLog.setIp("127.0.0.1");
		}
		//获取登录用户信息
		if(LoginUser!=null){
			sysLog.setUserId(LoginUser.getUserId());
			sysLog.setUserName(LoginUser.getUserName());
		}
		sysLog.setCreateTime(DateUtils.dateToStr(new Date(),DateUtils.FORMAT));
		//保存系统日志
		sysLogMapper.insert(sysLog);
	}

	@Override
	public void setLoginUser(SysUser user){
		 this.LoginUser = user;
	}
	//@Override
	//public SysUser getLoginUser(){
	//	return LoginUser;
	//}

	@Override
	public SysUser getUserByName(String username) {
		if(ConvertUtils.isEmpty(username)) {
			return null;
		}
		SysUser loginUser = new SysUser();
		SysUser sysUser = userMapper.getUserByName(username);
		if(sysUser==null) {
			return null;
		}
		BeanUtils.copyProperties(sysUser, loginUser);
		return loginUser;
	}


	@Override
	public List<String> getRolesByUsername(String username) {
		return sysUserRoleMapper.getRoleByUserName(username);
	}


	@Override
	public List<SysParam> queryParamsByParentId(String parentId) {
		return sysParamService.queryParamsByParentId(parentId);
	}

}
