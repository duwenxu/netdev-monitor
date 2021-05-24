package com.xy.netdev.sendrecv.entity.device;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Comtech协议对象数据体DTO
 *
 * @author duwenxu
 * @create 2021-05-20 17:03
 */
@Setter
@Getter
@Builder
public class ComtechEntity {

    @ApiModelProperty(value = "起始字节", notes = "STX")
    private byte start;

    @ApiModelProperty(value = "地址字节", notes = "address")
    private byte address;

    @ApiModelProperty(value = "指令字节", notes = "command")
    private byte[] command;

    @ApiModelProperty(value = "参数字节", notes = "parameters")
    private byte[] parameters;

    @ApiModelProperty(value = "结束字节",notes = "EXT")
    private byte end;

    @ApiModelProperty(value = "校验字节", notes = "checkByte")
    private byte check;
}
