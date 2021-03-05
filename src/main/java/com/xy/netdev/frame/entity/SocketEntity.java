package com.xy.netdev.frame.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SocketEntity {
    @ApiModelProperty("本地端口")
    private Integer localPort;

    @ApiModelProperty("远程端口")
    private Integer remotePort;

    @ApiModelProperty("远程地址")
    private String remoteAddress;

    @ApiModelProperty("原始收字节数据")
    private byte[] originalReceiveBytes;

    @ApiModelProperty("参数部分")
    private byte[] paramsDataBytes;

    @ApiModelProperty("发数据")
    private byte[] sendBytes;
}
