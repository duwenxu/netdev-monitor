package com.xy.netdev.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;

import com.xy.common.annotation.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 角色信息
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("XY_SYS_ROLE")
@ApiModel(value = "SysRole对象", description = "角色信息")
public class SysRole extends Model<SysRole> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "角色ID")
    @TableId(value = "ROLE_ID", type = IdType.AUTO)
    private Integer roleId;

    @ApiModelProperty(value = "角色名称")
    @TableField("ROLE_NAME")
    private String roleName;

    @ApiModelProperty(value = "角色状态")
    @TableField("ROLE_STATUS")
    private String roleStatus;

    @ApiModelProperty(value = "角色描述")
    @TableField("ROLE_DESC")
    private String roleDesc;

    @ApiModelProperty(value = "创建日期")
    @TableField("ROLE_DATE")
    private String roleDate;

    @User
    @ApiModelProperty(value = "创建人")
    @TableField("ROLE_UESRID")
    private Integer roleUesrid;


    @Override
    protected Serializable pkVal() {
        return this.roleId;
    }

}
