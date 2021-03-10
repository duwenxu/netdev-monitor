package com.xy.netdev.admin.entity;

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
 * 用户角色信息
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("XY_SYS_USER_ROLE")
@ApiModel(value = "SysUserRole对象", description = "用户角色信息")
public class SysUserRole extends Model<SysUserRole> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "角色ID")
    @TableId("ROLE_ID")
    private Integer roleId;

    @ApiModelProperty(value = "用户ID")
    @TableField("USER_ID")
    private Integer userId;


    @Override
    protected Serializable pkVal() {
        return this.roleId;
    }

}
