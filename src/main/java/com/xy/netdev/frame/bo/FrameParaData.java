package com.xy.netdev.frame.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 数据帧参数数据
 * </p>
 *
 * @author tangxl
 * @since 2021-03-10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FrameParaData {

    @ApiModelProperty(value = "参数编号")
    private String paraNo;

    @ApiModelProperty(value = "参数值")
    private String paraVal;

    @ApiModelProperty(value = "参数设置响应值")
    private String paraSetRes;
    /**
     * 参数表中 0020
     */
    @ApiModelProperty(value = "设备类型")
    private String devType;

    @ApiModelProperty(value = "设备编号")
    private String devNo;

    @ApiModelProperty(value = "参数长度")
    private Integer len;
}
