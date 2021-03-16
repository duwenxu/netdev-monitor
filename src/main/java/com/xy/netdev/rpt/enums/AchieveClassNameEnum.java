package com.xy.netdev.rpt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AchieveClassNameEnum {
    PARAM_QUERY("参数查询命令","paramQueryImpl"),
    PARAM_SET("参数设置命令","ParamSetImpl"),
    PARAM_WARN("参数警告命令","ParamWarnImpl"),
    REPORT_STATUS("状态上报","ReportStatusImpl"),
    REPORT_WARN("告警时间上报","ReportWarnImpl")
    ;

    private final String classNameZh;
    private final String className;
}
