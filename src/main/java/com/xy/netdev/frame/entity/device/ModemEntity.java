package com.xy.netdev.frame.entity.device;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 调制解调器头
 * @author cc
 */
@Setter
@Getter
@Builder
public class ModemEntity {

    @ApiModelProperty("起始字节")
    private Byte beginOffset;

    @ApiModelProperty("字节个数")
    private byte[] num;

    @ApiModelProperty("设备类型")
    private Byte deviceType;

    @ApiModelProperty("设备地址")
    private Byte deviceAddress;

    @ApiModelProperty("命令")
    private Byte cmd;

    @ApiModelProperty("参数")
    private byte[] params;

    @ApiModelProperty("校验字节")
    private Byte check;


    @ApiModelProperty("结束字节")
    private Byte end;

}
