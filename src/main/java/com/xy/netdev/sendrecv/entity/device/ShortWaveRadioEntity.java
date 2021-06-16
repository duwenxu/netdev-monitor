package com.xy.netdev.sendrecv.entity.device;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


/**
 * 712短波电台设备协议头对象
 */
@Setter
@Getter
@Builder
public class ShortWaveRadioEntity {

    @ApiModelProperty(value = "类型ID",notes = "2byte")
    private byte[] typeCmk;

    @ApiModelProperty(value = "信息长度",notes = "2byte")
    private byte[] infoLen;

    @ApiModelProperty("数据内容")
    private byte[] data;

    @ApiModelProperty(value = "校验字节",notes = "2byte")
    private byte[] check;
}
