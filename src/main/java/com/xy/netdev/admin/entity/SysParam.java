package com.xy.netdev.admin.entity;

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
 * 参数信息
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("XY_SYS_PARAM")
@ApiModel(value = "SysParam对象", description = "参数信息")
public class SysParam extends Model<SysParam> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "参数代码")
    @TableId("PARA_CODE")
    private String paraCode;

    @ApiModelProperty(value = "参数名称")
    @TableField("PARA_NAME")
    private String paraName;

    @ApiModelProperty(value = "参数父代码")
    @TableField("PARA_PARENT_ID")
    private String paraParentId;

    @ApiModelProperty(value = "备注一")
    @TableField("REMARK1")
    private String remark1;

    @ApiModelProperty(value = "备注二")
    @TableField("REMARK2")
    private String remark2;

    @ApiModelProperty(value = "备注三")
    @TableField("REMARK3")
    private String remark3;

    @Param
    @ApiModelProperty(value = "是否有效")
    @TableField("IS_VALIDATE")
    private String isValidate;


    @Override
    protected Serializable pkVal() {
        return this.paraCode;
    }

}
