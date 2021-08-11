package com.xy.netdev.sendrecv.entity.device;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 车载7.3mAcu协议传输帧对象
 *
 * @author duwenxu
 * @create 2021-08-09 11:24
 */
@Getter
@Setter
@Builder
public class AcuNewEntity {

    @ApiModelProperty(value = "数据包标识",notes = "2字节")
    private byte[] packageSign;

    @ApiModelProperty("信源地址")
    private Byte source;

    @ApiModelProperty("信宿地址")
    private Byte dest;

    @ApiModelProperty(value = "保留字",notes = "2字节")
    private byte[] retain;

    @ApiModelProperty("数据类别")
    private Byte dataType;

    @ApiModelProperty(value = "正文长度",notes = "4字节")
    private byte[] len;

    @ApiModelProperty("正文数据")
    private byte[] data;
}
