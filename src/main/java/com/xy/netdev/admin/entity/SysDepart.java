package com.xy.netdev.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.xy.common.annotation.Param;
import com.xy.common.annotation.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 部门信息
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("XY_SYS_DEPART")
@ApiModel(value = "SysDepart对象", description = "部门信息")
public class SysDepart extends Model<SysDepart> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "部门ID")
    @TableId(value = "DEPART_ID", type = IdType.AUTO)
    private Integer departId;

    @ApiModelProperty(value = "部门名称")
    @TableField("DEPART_NAME")
    private String departName;

    @ApiModelProperty(value = "部门代号")
    @TableField("DEPART_CODE")
    private String departCode;

    @Param
    @ApiModelProperty(value = "部门状态")
    @TableField("DEPART_STATUS")
    private String departStatus;

    @ApiModelProperty(value = "创建日期")
    @TableField("DEPART_DATE")
    private String departDate;

    @User
    @ApiModelProperty(value = "创建人")
    @TableField("DEPART_UESRID")
    private Integer departUesrid;

    @ApiModelProperty(value = "备注一")
    @TableField("REMARK1")
    private String remark1;

    @ApiModelProperty(value = "备注二")
    @TableField("REMARK2")
    private String remark2;

    @ApiModelProperty(value = "备注三")
    @TableField("REMARK3")
    private String remark3;


    @Override
    protected Serializable pkVal() {
        return this.departId;
    }

}
