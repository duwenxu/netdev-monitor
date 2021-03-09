package com.xy.netdev.monitor.bo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 设备参数扩展类
 * </p>
 *
 * @author tangxl
 * @since 2021-03-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "ParaViewInfo", description = "设备参数扩展类")
public class ParaViewInfo {
    /**
     *  下拉框显示数据,当显示类型 是1  A下拉框时,需要赋值下拉框列表
     */
    @ApiModelProperty(value = "下拉框显示数据")
    private List<ParaSpinnerInfo> spinnerInfoList = new ArrayList<>();

}
