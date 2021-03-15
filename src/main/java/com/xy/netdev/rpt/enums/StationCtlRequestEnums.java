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

    PARA_QUERY("参数查询命令", "0003"),
    PARA_QUERY_RESPONSE("参数查询响应", "0004"),
    PARA_SET("参数设置命令", "0005"),
    PARA_SET_RESPONSE("参数设置响应", "0006");

    private final String cmdName;
    private final String cmdCode;
}
