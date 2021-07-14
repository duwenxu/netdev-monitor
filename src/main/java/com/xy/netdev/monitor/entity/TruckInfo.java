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
@TableName("NTDV_TRUCK_INFO")
@ApiModel(value="TruckInfo对象", description="卫通车信息")
public class TruckInfo extends Model<PrtclFormat> {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "卫通车ID")
    @TableId(value = "TRUCK_ID", type = IdType.AUTO)
    private Integer truckId;

    @ApiModelProperty(value = "卫通车类型")
    @TableField(value = "TRUCK_TYPE")
    @Param
    private String truckType;

    @ApiModelProperty(value = "卫通车名称")
    @TableField(value = "TRUCK_NAME")
    private String truckName;

    @ApiModelProperty(value = "卫通车所属机构")
    @TableField(value = "TRUCK_DEPT")
    private String truckDept;

    @ApiModelProperty(value = "卫通车状态")
    @TableField(value = "TRUCK_STATUS")
    @Param
    private String truckStatus;

    @ApiModelProperty(value = "卫通车设备")
    @TableField(value = "TRUCK_DEVS")
    private String truckDevs;


    @Override
    protected Serializable pkVal() {
        return this.truckId;
    }

}

