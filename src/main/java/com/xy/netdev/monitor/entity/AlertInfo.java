package com.xy.netdev.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.xy.common.annotation.Param;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;

/**
 * 告警信息
 *
 * @author admin
 * @date 2021-03-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@TableName("NTDV_ALERT_INFO")
@Builder
@ApiModel(value="AlertInfo对象", description="告警信息")
public class AlertInfo extends Model<AlertInfo> {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "告警ID")
    @TableId(value = "ALERT_ID", type = IdType.AUTO)
    private Integer alertId;

    @Param
    @ApiModelProperty(value = "设备类型")
    @TableField(value = "DEV_TYPE")
    private String devType;

    @ApiModelProperty(value = "设备编号")
    @TableField(value = "DEV_NO")
    private String devNo;

    @ApiModelProperty(value = "参数编号")
    @TableField(value = "NDPA_NO")
    private String ndpaNo;

    @ApiModelProperty(value = "告警个数")
    @TableField(value = "ALERT_NUM")
    private Integer alertNum;

    @ApiModelProperty(value = "告警时间")
    @TableField(value = "ALERT_TIME")
    private String alertTime;

    @ApiModelProperty(value = "站号")
    @TableField(value = "ALERT_STATION_NO")
    private String alertStationNo;

    @ApiModelProperty(value = "告警级别")
    @TableField(value = "ALERT_LEVEL")
    @Param
    private String alertLevel;

    @ApiModelProperty(value = "告警描述")
    @TableField(value = "ALERT_DESC")
    private String alertDesc;


    @Override
    protected Serializable pkVal() {
        return this.alertId;
    }

}
