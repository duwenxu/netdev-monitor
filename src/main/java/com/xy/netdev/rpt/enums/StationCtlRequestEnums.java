package com.xy.netdev.rpt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @Description: 站控请求命令枚举
 * @Date 11:28 2021/3/15
 * @Author duwx
 */
@AllArgsConstructor
@Getter
@ToString
public enum StationCtlRequestEnums {

    DEV_AUTO_REPORT("设备参数主动上报","0","0000"),
    DEV_STATUS_REPORT ("设备状态上报", "1","0001"),
    PARA_ALARM_REPORT ("告警事件上报", "2","0002"),
    PARA_QUERY("参数查询命令", "3","0003"),
    PARA_QUERY_RESPONSE("参数查询响应", "4","0004"),
    PARA_SET("参数设置命令", "5","0005"),
    PARA_SET_RESPONSE("参数设置响应", "6","0006"),
    PARA_WARNING_QUERY("参数告警查询命令", "7","0007"),
    PARA_WARNING_QUERY_RESP("参数告警查询响应", "8","0008");

    private final String cmdName;
    private final String cmdCode;
    //信息类别
    private final String type;
}
