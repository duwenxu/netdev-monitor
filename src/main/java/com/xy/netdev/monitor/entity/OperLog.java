package com.xy.netdev.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 操作日志信息
 *
 * @author admin
 * @date 2021-03-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@TableName("NTDV_OPER_LOG")
@ApiModel(value="OperLog对象", description="操作日志信息")
public class OperLog extends Model<OperLog> {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "日志ID")
    @TableId(value = "LOG_ID", type = IdType.AUTO)
    private Integer logId;

    @ApiModelProperty(value = "设备类型")
    @TableField(value = "DEV_TYPE")
    private String devType;

    @ApiModelProperty(value = "设备编号")
    @TableField(value = "DEV_NO")
    private String devNo;

    @ApiModelProperty(value = "日志时间")
    @TableField(value = "LOG_TIME")
    private String logTime;

    @ApiModelProperty(value = "访问类型")
    @TableField(value = "LOG_ACCESS_TYPE")
    private String logAccessType;

    @ApiModelProperty(value = "操作类型")
    @TableField(value = "LOG_OPER_TYPE")
    private String logOperType;

    @ApiModelProperty(value = "操作对象")
    @TableField(value = "LOG_OPER_OBJ")
    private Integer logOperObj;

    @ApiModelProperty(value = "操作内容")
    @TableField(value = "LOG_OPER_CONTENT")
    private String logOperContent;

    @ApiModelProperty(value = "操作人")
    @TableField(value = "LOG_USER_ID")
    private Integer logUserId;


    @Override
    protected Serializable pkVal() {
        return this.logId;
    }

}
