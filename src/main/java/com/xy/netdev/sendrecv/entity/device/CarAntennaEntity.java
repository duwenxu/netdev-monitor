package com.xy.netdev.sendrecv.entity.device;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 车载卫星天线协议头对象
 * @author luo
 * @date 2021/4/7
 */

@Setter
@Getter
@Builder
public class CarAntennaEntity {

    @ApiModelProperty("起始字节")
    private byte beginOffset;

    @ApiModelProperty("设备类型")
    private byte deviceType;

    @ApiModelProperty("设备型号")
    private byte devSubType;

    @ApiModelProperty("设备地址")
    private byte[] deviceAddress;

    @ApiModelProperty("长度")
    private byte[] length;

    @ApiModelProperty("命令字")
    private byte cmd;

    @ApiModelProperty("参数体")
    private byte[] params;

    @ApiModelProperty("校验字")
    private byte check;

    @ApiModelProperty("结束符")
    private byte end;
}
