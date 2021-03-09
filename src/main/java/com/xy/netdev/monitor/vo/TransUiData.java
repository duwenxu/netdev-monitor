package com.xy.netdev.monitor.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 前端穿梭框组件数据格式拼装对象
 * </p>
 *
 * @author sunchao
 * @since 2021-03-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "穿梭框数据对象", description = "穿梭框数据对象")
public class TransUiData {

    @ApiModelProperty(value = "Id")
    private String id;

    @ApiModelProperty(value = "值")
    private String value;

    @ApiModelProperty(value = "是否选择")
    private Boolean isSelect;

    @ApiModelProperty(value = "值二")
    private String value2;

    @ApiModelProperty(value = "值三")
    private String value3;
}
