package com.xy.netdev.common.constant;


/**
 * 系统配置常量*
 *
 * @author tangxlf
 * @date 2018-8-11
 */
public interface SysConfigConstant {


    /**
     * 设备状态： 新品
     */
     String DEV_STATUS_NEW = "0028001";
    /**
     * 设备状态： 维修
     */
     String DEV_STATUS_REPAIR = "0028002";


    /**
     * 系统日志类型： 登录
     */
     String LOG_TYPE_LOGIN = "0098001";

    /**
     * 系统日志类型： 操作
     */
     String LOG_TYPE_LOGOUT = "0098002";

    /**
     * 自定义标识
     */
     String CUSTOMIZE = "自定义";

    /**
     * 顶级菜单ID
     */
     Integer TOP_MENU_ID = 0;

    /**
     *  0：一级菜单
     */
     Integer MENU_TYPE_0  = 0;
    /**
     *  1：子菜单
     */
     Integer MENU_TYPE_1  = 1;
    /**
     *  2：按钮权限
     */
     Integer MENU_TYPE_2  = 2;

    /**
     * 操作日志类型
     */
     String OPERATE_TYPE = "0099";

    /**
     * 操作日志类型： 查询
     */
     String OPERATE_TYPE_QUERY = "0099004";

    /**
     * 操作日志类型： 添加
     */
     String OPERATE_TYPE_ADD = "0099001";

    /**
     * 操作日志类型： 更新
     */
     String OPERATE_TYPE_UPDATE = "0099003";

    /**
     * 操作日志类型： 删除
     */
     String OPERATE_TYPE_DELETE = "0099002";

    /**
     * 操作日志类型： 导入
     */
     String OPERATE_TYPE_5 = "0099005";

    /**
     * 操作日志类型： 导出
     */
     String OPERATE_TYPE_6 = "0099006";

    /**
     * {@code 500 Server Error} (HTTP/1.0 - RFC 1945)
     */
     Integer SC_INTERNAL_SERVER_ERROR_500 = 500;

    /**
     * {@code 200 OK} (HTTP/1.0 - RFC 1945)
     */
     Integer SC_OK_200 = 200;

    /**
     * 参数名称翻译文本后缀
     */
     String PARA_NAME_SUFFIX = "_paraName";

    /**
     * 用户名称翻译文本后缀
     */
     String USER_NAME_SUFFIX = "_userName";

    /**
     * 用户名称翻译文本后缀
     */
     String ORG_NAME_SUFFIX = "_orgName";

    /**
     * 状态--有效
     */
     String STATUS_OK = "0001001";

    /**
     * 状态--无效
     */
     String STATUS_FAIL = "0001002";

    /***
     * 是否   0003001 TRUE
     */
     String  IS_DEFAULT_TRUE="0003001";
    /***
     * 是否   0003002 FALSE
     */
     String  IS_DEFAULT_FALSE="0003002";
    /***
     * 公共参数--设备日志信息显示条数
     */
    String  DEV_LOG_VIEW_SZIE="0100004";

    /***
     * 公共参数--设备告警信息保存条数
     */
    String  DEV_ALERT_INFO_SZIE="0100005";

    /***
     * 设备类型父节点
     */
    String  DEV_TYPE_PARENT="0020";
}
