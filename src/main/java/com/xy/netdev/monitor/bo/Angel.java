package com.xy.netdev.monitor.bo;

import com.xy.netdev.monitor.entity.Interface;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 地面站和卫星经纬度实体类
 * </p>
 *
 * @author sunchao
 * @since 2021-05-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "地面站和卫星经纬度实体类", description = "地面站和卫星经纬度实体类")
public class  Angel {

    @ApiModelProperty(value = "设备序号")
    private String devNo;

    @ApiModelProperty(value = "功能(星下点、空间指向)")
    private String func;

    @ApiModelProperty(value = "卫星经度")
    private String satJd;

    @ApiModelProperty(value = "卫星纬度")
    private String satWd;

    @ApiModelProperty(value = "地面站经度")
    private String devJd;

    @ApiModelProperty(value = "地面纬度")
    private String devWd;


    @ApiModelProperty(value = "是否水平")
    private Boolean isLevel;

    @ApiModelProperty(value = "频率")
    private String freq;

    @ApiModelProperty(value = "方位")
    private String az = "0";

    @ApiModelProperty(value = "俯仰")
    private String el = "0";

    @ApiModelProperty(value = "交叉")
    private String jc= "0";

    @ApiModelProperty(value = "极化")
    private String pol = "0";

    /**
     * 1:方位 2：俯仰 3：交叉  4：极化
     */
    @ApiModelProperty(value = "步进类型")
    private String stepType = "0";
}
