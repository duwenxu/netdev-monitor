package com.xy.netdev.monitor.bo;


import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 供页面展示的下拉框列表信息
 * </p>
 *
 * @author tangxl
 * @since 2021-03-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "ParaSpinnerInfo对象", description = "下拉框列表信息")
public class ParaSpinnerInfo extends Model<ParaSpinnerInfo>  {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "下拉框选项编码")
    private String code;

    @ApiModelProperty(value = "下拉框选项名称")
    private String name;

}
