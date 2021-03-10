package com.xy.netdev.frame.bo;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * <p>
 * 协议解析请求数据
 * </p>
 *
 * @author tangxl
 * @since 2021-03-10
 */
public class FrameReqData {

    @ApiModelProperty(value = "命令标识符")
    private Integer cmdMark;
    /**
     * 参数表中 0020
     */
    @ApiModelProperty(value = "设备类型")
    private String devType;

    @ApiModelProperty(value = "设备编号")
    private String devNo;

    @ApiModelProperty(value = "帧参数列表")
    private List<FrameParaData> frameParaList;
}
