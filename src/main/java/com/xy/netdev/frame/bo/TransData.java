package com.xy.netdev.frame.bo;

import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * 协议解析输入数据
 * </p>
 *
 * @author tangxl
 * @since 2021-03-10
 */
public class TransData {

    @ApiModelProperty(value = "参数编号")
    private String paraNo;

    @ApiModelProperty(value = "参数值")
    private String paraVal;

    @ApiModelProperty(value = "命令标识符")
    private Integer cmdMark;


    @ApiModelProperty(value = "设备编号")
    private String devNo;
}
