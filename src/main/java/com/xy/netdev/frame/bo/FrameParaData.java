package com.xy.netdev.frame.bo;

import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * 数据帧参数数据
 * </p>
 *
 * @author tangxl
 * @since 2021-03-10
 */
public class FrameParaData {

    @ApiModelProperty(value = "参数编号")
    private String paraNo;

    @ApiModelProperty(value = "参数值")
    private String paraVal;
    /**
     * 参数表中 0020
     */
    @ApiModelProperty(value = "设备类型")
    private String devType;

    @ApiModelProperty(value = "设备编号")
    private String devNo;
}
