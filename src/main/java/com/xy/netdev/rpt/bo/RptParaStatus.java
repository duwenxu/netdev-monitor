package com.xy.netdev.rpt.bo;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RptParaStatus {

    /**
     * 参数编号
     */
    private String paramNo;

    /**
     * 上报类型
     */
    private String rptType;

    /**
     * 当前状态
     */
    private String status;
}
