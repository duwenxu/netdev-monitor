package com.xy.netdev.SpacePreset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 卫星预置
 * </p>
 *
 * @author zb
 * @since 2021-06-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("NTDV_SPACE_PRESET")
@ApiModel(value = "NtdvSpacePreset对象", description = "卫星预置")
public class NtdvSpacePreset extends Model<NtdvSpacePreset> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预置ID")
    @TableId(value = "SP_ID", type = IdType.AUTO)
    private Integer spId;

    @ApiModelProperty(value = "预置卫星代号")
    @TableField("SP_CODE")
    private String spCode;

    @ApiModelProperty(value = "预置卫星名称")
    @TableField("SP_NAME")
    private String spName;

    @ApiModelProperty(value = "卫星经度")
    @TableField("SP_LONGITUDE")
    private String spLongitude;

    @ApiModelProperty(value = "本振")
    @TableField("SP_LOCAL_OSCILLATOR")
    private String spLocalOscillator;

    @ApiModelProperty(value = "信标频率")
    @TableField("SP_BEACON_FREQUENCY")
    private String spBeaconFrequency;

    @ApiModelProperty(value = "极化方向")
    @TableField("SP_POLARIZATION")
    private String spPolarization;

    @ApiModelProperty(value = "备注一")
    @TableField("SP_REMARK1")
    private String spRemark1;

    @ApiModelProperty(value = "备注二")
    @TableField("SP_REMARK2")
    private String spRemark2;

    @ApiModelProperty(value = "备注三")
    @TableField("SP_REMARK3")
    private String spRemark3;

    @ApiModelProperty(value = "备注四")
    @TableField("SP_REMARK4")
    private String spRemark4;

    @Override
    protected Serializable pkVal() {
        return this.spId;
    }

}
