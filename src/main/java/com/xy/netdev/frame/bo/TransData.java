package com.xy.netdev.frame.bo;

import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * 协议解析传输数据
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
    /**
     * 参数表中 0020
     */
    @ApiModelProperty(value = "设备类型")
    private String devType;

    @ApiModelProperty(value = "设备编号")
    private String devNo;
}
