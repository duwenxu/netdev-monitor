package com.xy.netdev.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
    /**
     * 前端显示
     */
    @ApiModelProperty(value = "日志时间")
    @TableField(value = "LOG_TIME")
    private String logTime;

    /**
     * 参数表中  0025001 参数  0025002 接口
     */
    @ApiModelProperty(value = "访问类型")
    @TableField(value = "LOG_ACCESS_TYPE")
    private String logAccessType;

    /**
     * 前端显示
     */
    @ApiModelProperty(value = "访问类型名称")
    @TableField(exist = false)
    private String logAccessTypeName;

    @ApiModelProperty(value = "操作类型")
    @TableField(value = "LOG_OPER_TYPE")
    private String logOperType;

    /**
     * 前端显示
     */
    @ApiModelProperty(value = "操作类型名称")
    @TableField(exist = false)
    private String logOperTypeName;

    @ApiModelProperty(value = "操作对象")
    @TableField(value = "LOG_OPER_OBJ")
    private Integer logOperObj;

    /**
     * 前端显示
     */
    @ApiModelProperty(value = "命令标识符")
    @TableField(value = "LOG_CMD_MARK")
    private String logCmdMark;

    /**
     * 前端显示
     */
    @ApiModelProperty(value = "操作对象名称")
    @TableField(value = "LOG_OPER_OBJ_NAME")
    private String logOperObjName;

    /**
     * 前端显示
     */
    @ApiModelProperty(value = "操作内容")
    @TableField(value = "LOG_OPER_CONTENT")
    private String logOperContent;

    @ApiModelProperty(value = "操作人")
    @TableField(value = "LOG_USER_ID")
    private Integer logUserId;

    /**
     * 前端显示
     */
    @ApiModelProperty(value = "原始数据")
    @TableField(value = "ORGIN_DATA")
    private String orignData;


    @Override
    protected Serializable pkVal() {
        return this.logId;
    }

}
