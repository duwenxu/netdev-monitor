package com.xy.netdev.monitor.bo;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 内外数据转换映射规则
 * </p>
 *
 * @author luo
 * @since 2021-03-11
 */
@Data
public class TransRule {

    @ApiModelProperty(value = "设备内部参数值")
    private String inner;

    @ApiModelProperty(value = "转换到代理设备状态值")
    private String outer;
}
