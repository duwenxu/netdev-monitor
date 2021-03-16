package com.xy.netdev.rpt.bo;

import com.xy.netdev.rpt.enums.AchieveClassNameEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;


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

    @ApiModelProperty(value = "实现类")
    @NotNull
    private AchieveClassNameEnum achieveClassNameEnum;
}
