package com.xy.netdev.monitor.bo;


import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 设备状态信息
 * </p>
 *
 * @author tangxl
 * @since 2021-03-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "DevStatusInfo对象", description = "设备状态信息")
public class DevStatusInfo extends Model<DevStatusInfo>  {

    private static final long serialVersionUID = 1L;

    /**
     * 0:正常|1:中断
     */
    @ApiModelProperty(value = "是否中断")
    private String isInterrupt;
    /**
     * 0:无告警|1:告警
     */
    @ApiModelProperty(value = "是否告警")
    private String isAlarm;
    /**
     * 0:不启用主备|1:启用主备
     */
    @ApiModelProperty(value = "是否启用主备")
    private String isUseStandby;
    /**
     * 0:主用|1:备用
     */
    @ApiModelProperty(value = "主用还是备用")
    private String masterOrSlave;
    /**
     * 0:正常状态|1:维修状态
     */
    @ApiModelProperty(value = "工作状态")
    private String workStatus;

}