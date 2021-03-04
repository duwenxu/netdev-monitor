package com.xy.netdev.admin.api;


import com.xy.netdev.admin.entity.SysParam;
import com.xy.netdev.admin.entity.SysUser;

import java.util.List;

/**
 * @Description: 底层共通业务API，提供其他独立模块调用
 * @Date:2019-9-17
 */
public interface ISysBaseAPI {

	/**
	 * 日志添加
	 * @param LogContent 内容
	 * @param logType 日志类型(0:操作日志;1:登录日志;2:定时任务)
	 * @param operatetype 操作类型(1:添加;2:修改;3:删除;)
	 */
	void addLog(String LogContent, String logType, String operatetype);


	/**
	 * 获取当前登录用户信息
	 * @param
	 * @return
	 */
	public void setLoginUser(SysUser user);

	/**
	 * 获取当前登录用户信息
	 * @param
	 * @return
	 */
	public SysUser getLoginUser();

	/**
	  * 根据用户账号查询登录用户信息
	 * @param username
	 * @return
	 */
	public SysUser getUserByName(String username);
	
	/**
	 * 通过用户账号查询角色集合
	 * @param username
	 * @return
	 */
	public List<String> getRolesByUsername(String username);

	/**
	  * 获取参数数据
	 * @param parentId
	 * @return
	 */
	public List<SysParam> queryParamsByParentId(String parentId);

}
