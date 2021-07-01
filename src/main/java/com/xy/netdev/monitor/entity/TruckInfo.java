package com.xy.netdev.monitor.entity;


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
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 设备车信息
 *
 * @author admin
 * @date 2021-03-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@TableName("NTDV_PRTCL_FORMAT")
@ApiModel(value="PrtclFormat对象", description="协议格式")
public class TruckInfo extends Model<PrtclFormat> {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "通讯车ID")
    @TableId(value = "TRUCK_ID", type = IdType.AUTO)
    private Integer truckId;

    @ApiModelProperty(value = "通讯车类型")
    @TableField(value = "TRUCK_TYPE")
    @Param
    private String truckType;

    @ApiModelProperty(value = "通讯车名称")
    @TableField(value = "TRUCK_NAME")
    private String truckName;

    @ApiModelProperty(value = "通讯车状态")
    @TableField(value = "TRUCK_STATUS")
    private String truckStatus;

    @ApiModelProperty(value = "通讯车设备")
    @TableField(value = "TRUCK_DEVS")
    private String truckDevs;


    @Override
    protected Serializable pkVal() {
        return this.truckId;
    }

}

