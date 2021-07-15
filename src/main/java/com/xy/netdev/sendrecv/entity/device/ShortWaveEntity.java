package com.xy.netdev.sendrecv.entity.device;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


/**
 * 750-400W短波设备协议头对象
 */
@Setter
@Getter
@Builder
public class ShortWaveEntity {

    @ApiModelProperty(value = "报头",notes = "2byte")
    private byte[] frameHead;

    @ApiModelProperty(value = "命令字")
    private Byte cmk;

    @ApiModelProperty("数据内容")
    private byte[] data;

    @ApiModelProperty(value = "序号",notes = "4byte")
    private byte[] serialNum;

    @ApiModelProperty(value = "校验字节",notes = "2byte")
    private byte[] crc;
}
