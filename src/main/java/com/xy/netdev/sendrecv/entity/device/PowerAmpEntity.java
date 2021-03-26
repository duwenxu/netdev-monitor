package com.xy.netdev.sendrecv.entity.device;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


/**
 * Ku4000W功率放大器 包装结构
 */
@Setter
@Getter
@Builder
public class PowerAmpEntity {

    @ApiModelProperty("起始字节")
    private Byte beginOffset;

    @ApiModelProperty("设备类型")
    private Byte devType;

    @ApiModelProperty("设备型号")
    private Byte devSubType;

    @ApiModelProperty("设备地址")
    private byte[] deviceAddress;

    @ApiModelProperty("长度")
    private byte[] length;

    @ApiModelProperty("命令")
    private Byte cmd;

    @ApiModelProperty("参数")
    private byte[] params;

    @ApiModelProperty("校验字节")
    private Byte check;

    @ApiModelProperty("结束符")
    private Byte end;

}
