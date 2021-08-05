package com.xy.netdev.WorkPlan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.xy.common.annotation.Param;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 值班安排
 * </p>
 *
 * @author zb
 * @since 2021-06-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("NTDV_WORK_PLAN")
@ApiModel(value = "NtdvWorkPlan对象", description = "值班安排")
public class NtdvWorkPlan extends Model<NtdvWorkPlan> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "值班ID")
    @TableId(value = "WP_ID", type = IdType.AUTO)
    private Integer wpId;

    @ApiModelProperty(value = "计划值班人员")
    @TableField("WP_NAME")
    private String wpName;

    @ApiModelProperty(value = "值班开始时间")
    @TableField("WP_START_TIME")
    private String wpStartTime;

    @ApiModelProperty(value = "值班结束时间")
    @TableField("WP_END_TIME")
    private String wpEndTime;

    @ApiModelProperty(value = "实际值班人员")
    @TableField("WP_REAL_NAME")
    private String wpRealName;

    @ApiModelProperty(value = "值班天气")
    @TableField("WP_WEATHER")
    private String wpWeather;

    @ApiModelProperty(value = "值班接班时间")
    @TableField("WP_FLWIN_TIME")
    private String wpFlwinTime;

    @ApiModelProperty(value = "值班交班时间")
    @TableField("WP_FLWOUT_TIME")
    private String wpFlwoutTime;

//    @Param
    @ApiModelProperty(value = "值班备注")
    @TableField("WP_DESC")
    private String wpDesc;

    @Param
    @ApiModelProperty(value = "值班类型")
    @TableField("WP_STATUS")
    private String wpStatus;

    @Override
    protected Serializable pkVal() {
        return this.wpId;
    }

}
