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
    private String cmdMarkHexStr;

    @ApiModelProperty(value = "站号")
    private String stationNo;

    @ApiModelProperty(value = "设备数量")
    private Integer devNum;

    @ApiModelProperty(value = "数据体")
    private Object param;
}
