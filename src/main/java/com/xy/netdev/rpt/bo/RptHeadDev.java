package com.xy.netdev.rpt.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class RptHeadDev {

    @ApiModelProperty(value = "站控编号")
    private String devNo;

    @ApiModelProperty(value = "信息类别")
    private String cmdMark;

    @ApiModelProperty(value = "数据体")
    private Object param;
}
