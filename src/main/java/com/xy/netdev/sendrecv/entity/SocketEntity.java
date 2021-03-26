package com.xy.netdev.sendrecv.entity;

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

    @ApiModelProperty("数据体")
    private byte[] bytes;

}
