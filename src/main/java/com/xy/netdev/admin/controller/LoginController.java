package com.xy.netdev.admin.controller;

import com.alibaba.fastjson.JSONObject;
import com.xy.common.model.Result;
import com.xy.common.model.ResultBody;
import com.xy.common.util.ConvertUtils;
import com.xy.netdev.admin.api.ISysBaseAPI;
import com.xy.netdev.admin.entity.SysDepart;
import com.xy.netdev.admin.entity.SysUser;
import com.xy.netdev.admin.vo.SysLoginModel;
import com.xy.netdev.admin.service.ISysDepartService;
import com.xy.netdev.admin.service.ISysLogService;
import com.xy.netdev.admin.service.ISysUserService;
import com.xy.netdev.common.constant.SysConfigConstant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * @author luoqilong
 * @since 2019-09-17
 */
@RestController
@RequestMapping("/sys")
@Api(tags="用户登录")
@Slf4j
public class LoginController {

	@Autowired
	private ISysBaseAPI sysBaseAPI;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysLogService logService;
	@Autowired
    private ISysDepartService sysDepartService;



	@PostMapping(value = "/login")
	@ApiOperation("登录接口")
	public Result<JSONObject> login(SysLoginModel sysLoginModel) throws Exception {
		Result<JSONObject> result = new Result<JSONObject>();
		String username = sysLoginModel.getUserName();
		String password = sysLoginModel.getPassword();

		//1. 校验用户是否有效
		SysUser sysUser = sysUserService.getUserByName(username);
		result = sysUserService.checkUserIsEffective(sysUser);
		if(!result.isSuccess()) {
			return result;
		}
		//2. 校验用户名或密码是否正确
		//String userpassword = PasswordUtils.encrypt(username, password, sysUser.getUserSalt());
		String syspassword = sysUser.getUserPwd();
		if (!syspassword.equals(password)) {
			result.error500("用户名或密码错误");
			return result;
		}
		//用户登录信息
		userInfo(sysUser, result);
		sysBaseAPI.setLoginUser(sysUser);
		sysBaseAPI.addLog("用户名: " + username + ",登录成功！", SysConfigConstant.LOG_TYPE_LOGIN, null);
		return result;
	}


	/**
	 * 获取访问量
	 * @return
	 */
	@GetMapping("loginfo")
	public Result<JSONObject> loginfo() {
		Result<JSONObject> result = new Result<JSONObject>();
		JSONObject obj = new JSONObject();
		// 获取一天的开始和结束时间
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date dayStart = calendar.getTime();
		calendar.add(Calendar.DATE, 1);
		Date dayEnd = calendar.getTime();
		// 获取系统访问记录
		Long totalVisitCount = logService.findTotalVisitCount();
		obj.put("totalVisitCount", totalVisitCount);
		Long todayVisitCount = logService.findTodayVisitCount(dayStart,dayEnd);
		obj.put("todayVisitCount", todayVisitCount);
		Long todayIp = logService.findTodayIp(dayStart,dayEnd);
		obj.put("todayIp", todayIp);
		result.setResult(obj);
		result.success("登录成功");
		return result;
	}

	/**
	 * 获取访问量
	 * @return
	 */
	@GetMapping("visitInfo")
	public Result<List<Map<String,Object>>> visitInfo() {
		Result<List<Map<String,Object>>> result = new Result<List<Map<String,Object>>>();
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.HOUR_OF_DAY,0);
		calendar.set(Calendar.MINUTE,0);
		calendar.set(Calendar.SECOND,0);
		calendar.set(Calendar.MILLISECOND,0);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		Date dayEnd = calendar.getTime();
		calendar.add(Calendar.DAY_OF_MONTH, -7);
		Date dayStart = calendar.getTime();
		List<Map<String,Object>> list = logService.findVisitCount(dayStart, dayEnd);
		result.setResult(ConvertUtils.toLowerCasePageList(list));
		return result;
	}


	/**
	 * 登陆成功选择用户当前部门
	 * @param user
	 * @return
	 */
	@PutMapping(value = "/selectDepart")
	public Result<JSONObject> selectDepart(@RequestBody SysUser user) {
		Result<JSONObject> result = new Result<JSONObject>();
		String username = user.getUserName();
		Integer orgId = user.getUserOrgid();
		this.sysUserService.updateUserDepart(username, orgId);
		SysUser sysUser = sysUserService.getUserByName(username);
		JSONObject obj = new JSONObject();
		obj.put("userInfo", sysUser);
		result.setResult(obj);
		return result;
	}

	/**
	 * 用户信息
	 *
	 * @param sysUser
	 * @param result
	 * @return
	 */
	private Result<JSONObject> userInfo(SysUser sysUser, Result<JSONObject> result) {
		String syspassword = sysUser.getUserPwd();
		String username = sysUser.getUserName();
		// 获取用户部门信息
		JSONObject obj = new JSONObject();
		List<SysDepart> departs = sysDepartService.queryUserDeparts(sysUser.getUserOrgid());
		obj.put("departs", departs);
		obj.put("userInfo", sysUser);
		result.setResult(obj);
		result.success("登录成功");
		return result;
	}


}