package com.xy.netdev.admin.entity;

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
import java.util.List;

/**
 * <P>
 * 用户信息
 * </P>
 *
 * @author luoqilong
 * @since 2019-09-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("XY_SYS_USER")
@ApiModel(value = "SysUser对象", description = "用户信息")
public class SysUser extends Model<SysUser> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID")
    @TableId(value = "USER_ID", type = IdType.AUTO)
    private Integer userId;

    @ApiModelProperty(value = "姓名")
    @TableField("USER_NAME")
    private String userName;

    @ApiModelProperty(value = "英文名")
    @TableField("USER_CHNAME")
    private String userChname;

    @Param
    @ApiModelProperty(value = "用户类型")
    @TableField("USER_TYPE")
    private String userType;

    @ApiModelProperty(value = "机构ID")
    @TableField("USER_ORGID")
    private Integer userOrgid;

    @ApiModelProperty(value = "用户密码")
    @TableField("USER_PWD")
    private String userPwd;

    @ApiModelProperty(value = "md5密码盐")
    @TableField("USER_SALT")
    private String userSalt;

    @ApiModelProperty(value = "用户联系方式")
    @TableField("USER_CONTACT")
    private String userContact;

    @ApiModelProperty(value = "用户手机号")
    @TableField("USER_PHONE")
    private String userPhone;

    @ApiModelProperty(value = "用户邮箱")
    @TableField("USER_EMAIL")
    private String userEmail;

    @Param
    @ApiModelProperty(value = "用户状态")
    @TableField("USER_STATUS")
    private String userStatus;

    @ApiModelProperty(value = "创建日期")
    @TableField("USER_DATE")
    private String userDate;

    @ApiModelProperty(value = "用户角色")
    @TableField(exist = false)
    private List<SysRole> userRole;



    @Override
    protected Serializable pkVal() {
        return this.userId;
    }

}
