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
     * 设备部署类型--独立设备   0031001
     */
    String  DEV_DEPLOY_ALONE="0031001";

    /***
     * 设备参数复杂级别--简单参数
     */
    String  PARA_COMPLEX_LEVEL_SIMPLE="0019001";
    /***
     * 设备参数复杂级别--复杂参数
     */
    String  PARA_COMPLEX_LEVEL_COMPLEX="0019002";
    /***
     * 设备参数复杂级别--组合参数
     */
    String  PARA_COMPLEX_LEVEL_COMPOSE="0019003";
    /***
     * 设备参数复杂级别--子参数
     */
    String  PARA_COMPLEX_LEVEL_SUB="0019004";

    /***
     * 设备部署类型--主设备   0031002
     */
    String  DEV_DEPLOY_MASTER="0031002";
    /***
     * 设备部署类型--备设备   0031003
     */
    String  DEV_DEPLOY_SLAVE="0031003";
    /***
     * 设备部署类型--集群设备   0031004
     */
    String  DEV_DEPLOY_GROUP="0031004";
    /***
     * 设备使用状态--在用   0032001
     */
    String  DEV_USE_STATUS_INUSE="0032001";
    /***
     * 设备使用状态--不在用   0032001
     */
    String  DEV_USE_STATUS_NOTUSE="0032002";

    /***
     * 公共参数--查询间隔时间
     */
    String  DEV_QUERY_INTERVAL="0100002";

    /***
     * 公共参数--上报间隔时间
     */
    String  DEV_REPORT_INTERVAL="0100003";

    /***
     * 公共参数--设备日志信息显示条数
     */
    String  DEV_LOG_VIEW_SZIE="0100004";

    /***
     * 公共参数--设备告警信息保存条数
     */
    String  DEV_ALERT_INFO_SZIE="0100005";

    /**
     * 操作类型--查询
     */
    String OPREATE_QUERY = "0026001";

    /**
     * 操作类型--查询响应
     */
    String OPREATE_QUERY_RESP = "0026002";

    /**
     * 操作类型--控制
     */
    String OPREATE_CONTROL = "0026003";

    /**
     * 操作类型--控制响应
     */
    String OPREATE_CONTROL_RESP = "0026004";

    /**
     * 告警级别--正常
     */
    String ALERT_LEVEL_OK = "0021006";

    /**
     * 参数访问限制--只写
     */
    String  ONLY_WRITE= "0022001";

    /**
     * 参数访问限制--只读
     */
    String ONLY_READ = "0022002";

    /**
     * 参数访问限制--读写
     */
    String READ_WRITE = "0022003";
    /**
     * 参数访问限制--无权限
     */
    String NO_RIGHT = "0022004";

    /**
     * 参数访问限制--命令
     */
    String CMD_RIGHT = "0022005";

    /**
     * 访问类型--参数
     */
    String ACCESS_TYPE_PARAM = "0025001";

    /**
     * 访问类型--接口
     */
    String ACCESS_TYPE_INTERF = "0025002";

    /**
     * UDP
     */
    String UDP = "0030001";

    /**
     * TCP
     */
    String TCP = "0030002";

    /**
     * 设备状态--中断
     */
    String DEV_STATUS_INTERRUPT = "0029001";

    /**
     * 设备状态--告警
     */
    String DEV_STATUS_ALARM = "0029002";

    /**
     * 设备状态--启用主备
     */
    String DEV_STATUS_SWITCH = "0029003";

    /**
     * 设备状态--主备
     */
    String DEV_STATUS_STANDBY = "0029004";

    /**
     * 设备状态--维修
     */
    String DEV_STATUS_MAINTAIN = "0029005";

    /**
     * 设备状态--默认
     */
    String DEV_STATUS_DEFAULT = "0029006";

    /**
     * 公共参数--测站编号
     */
    String PUBLIC_PARA_STATION_NO = "0100001";

    /**
     * 公共参数--54所ID地址
     */
    String RPT_IP_ADDR = "0100006";

    /**
     * 站控-设备状态上报--中断状态-中断
     */
    String RPT_DEV_STATUS_ISINTERRUPT_YES = "1";


    /**
     * 站控-设备状态上报--中断状态-未中断
     */
    String RPT_DEV_STATUS_ISINTERRUPT_NO = "0";


    /**
     * 站控-设备状态上报--告警状态-告警
     */
    String RPT_DEV_STATUS_ISALARM_YES = "1";


    /**
     * 站控-设备状态上报--告警状态-未告警
     */
    String RPT_DEV_STATUS_ISALARM_NO = "0";


    /**
     * 站控-设备状态上报--启用主备状态-启用主备
     */
    String RPT_DEV_STATUS_USESTANDBY_YES = "1";


    /**
     * 站控-设备状态上报--启用主备状态-未启用主备
     */
    String RPT_DEV_STATUS_USESTANDBY_NO = "0";


    /**
     * 站控-设备状态上报--主备状态-备
     */
    String RPT_DEV_STATUS_MASTERORSLAVE_SLAVE = "1";


    /**
     * 站控-设备状态上报--主备状态-主
     */
    String RPT_DEV_STATUS_MASTERORSLAVE_MASTER = "0";

    /**
     * 站控-设备状态上报--工作状态-维修
     */
    String RPT_DEV_STATUS_WORKSTATUS_NO = "1";


    /**
     * 站控-设备状态上报--工作状态-正常
     */
    String RPT_DEV_STATUS_WORKSTATUS_YES = "0";

    /**
     * 调制解调器控制成功响应--code
     */
    String CONTROL_SUCCESS = "0061000";

    /**
     * 调制解调器控制失败响应--code
     */
    String CONTROL_FAIL = "0061001";

    /**
     * 参数上报设备状态类型--无
     */
    String PARA_ALERT_TYPE_NULL = "0029006";


}
