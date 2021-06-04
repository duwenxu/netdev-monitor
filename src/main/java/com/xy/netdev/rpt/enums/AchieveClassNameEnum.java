package com.xy.netdev.rpt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
public enum AchieveClassNameEnum {
    PARAM_QUERY("参数查询命令","paramQueryImpl"),
    PARAM_SET("参数设置命令","paramSetImpl"),
    PARAM_WARN("参数警告命令","paramWarnImpl"),
    REPORT_STATUS("设备状态上报","reportStatusImpl"),
    REPORT_WARN("告警事件上报","reportWarnImpl")
    ;

    private final String classNameZh;
    private final String clazzName;
}
