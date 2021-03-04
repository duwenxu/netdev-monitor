package com.xy.netdev.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 系统日志表
 * </p>
 *
 * @author luoqilong
 * @since 2019-09-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("XY_SYS_LOG")
@ApiModel(value = "SysLog对象", description = "系统日志表")
public class SysLog extends Model<SysLog> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private String id;

    @ApiModelProperty(value = "日志类型（1登录日志，2操作日志）")
    @TableField("LOG_TYPE")
    private String logType;

    @ApiModelProperty(value = "日志内容")
    @TableField("LOG_CONTENT")
    private String logContent;

    @ApiModelProperty(value = "操作类型")
    @TableField("OPERATE_TYPE")
    private String operateType;

    @ApiModelProperty(value = "操作用户账号")
    @TableField("USER_ID")
    private Integer userId;

    @ApiModelProperty(value = "操作用户名称")
    @TableField("USER_NAME")
    private String userName;

    @ApiModelProperty(value = "IP")
    @TableField("IP")
    private String ip;

    @ApiModelProperty(value = "请求java方法")
    @TableField("METHOD")
    private String method;

    @ApiModelProperty(value = "请求路径")
    @TableField("REQUEST_URL")
    private String requestUrl;

    @ApiModelProperty(value = "请求参数")
    @TableField("REQUEST_PARAM")
    private String requestParam;

    @ApiModelProperty(value = "耗时")
    @TableField("COST_TIME")
    private Long costTime;

    @ApiModelProperty(value = "创建时间")
    @TableField("CREATE_TIME")
    private String createTime;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
