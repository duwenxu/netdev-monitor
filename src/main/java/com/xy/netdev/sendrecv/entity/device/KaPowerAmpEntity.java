package com.xy.netdev.sendrecv.entity.device;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Ka频段100W发射机协议对象DTO
 *
 * @author duwenxu
 * @create 2021-04-30 14:26
 */
@Setter
@Getter
@Builder
public class KaPowerAmpEntity {

    @ApiModelProperty(value = "帧头", notes = "1byte")
    private byte head;

    @ApiModelProperty(value = "信息长度", notes = "1byte")
    private byte msgLen;

    @ApiModelProperty(value = "地址", notes = "1byte")
    private byte sad;

    @ApiModelProperty(value = "命令码",notes = "1byte")
    private byte cmd;

    @ApiModelProperty(value = "数据域")
    private byte[] data;

    @ApiModelProperty(value = "校验位", notes = "1byte")
    private byte check;

    @ApiModelProperty(value = "帧尾", notes = "1byte")
    private byte end;
}
