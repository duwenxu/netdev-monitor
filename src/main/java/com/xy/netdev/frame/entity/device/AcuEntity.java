package com.xy.netdev.frame.entity.device;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * acu天线
 * @author cc
 */
@Setter
@Getter
@Builder
public class AcuEntity {

    @ApiModelProperty("帧头")
    private Byte frameHead;

    @ApiModelProperty("状态字")
    private String stats;

    @ApiModelProperty("工作方式")
    private Byte workWay;

    @ApiModelProperty("方位引导角度")
    private Float azimuthGuide;

    @ApiModelProperty("俯仰引导角度")
    private Float pitchAngleGuide;

    @ApiModelProperty("极化引导角度")
    private Float polarizationAngleGuide;

    @ApiModelProperty("方位命令角度")
    private Float azimuthCommand;

    @ApiModelProperty("俯仰命令角度")
    private Float pitchAngleCommand;

    @ApiModelProperty("极化命令角度")
    private Float polarizationAngleCommand;

    @ApiModelProperty("俯仰上限")
    private Byte pitchUpperLimit;

    @ApiModelProperty("俯仰下限")
    private Byte pitchLowerLimit;

    @ApiModelProperty("接收机锁定状态")
    private Byte lockStatus;

    @ApiModelProperty("AGC值")
    private Float agc;

    @ApiModelProperty("经度")
    private Float longitude;

    @ApiModelProperty("纬度")
    private Float latitude;

    @ApiModelProperty("航向")
    private Float course;

    @ApiModelProperty("保留")
    private byte[] keep;

    @ApiModelProperty("帧尾")
    private Byte frameTail;

}
