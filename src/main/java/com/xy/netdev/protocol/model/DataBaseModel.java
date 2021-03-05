package com.xy.netdev.protocol.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 数据基类
 * @author cc
 */
@Setter
@Getter
public class DataBaseModel {

    @ApiModelProperty("本地端口")
    private Integer localPort;

    @ApiModelProperty("远程端口")
    private Integer remotePort;

    @ApiModelProperty("远程地址")
    private Integer remoteAddress;

    @ApiModelProperty("原始收字节数据")
    private byte[] originalReceiveBytes;

    @ApiModelProperty("参数部分")
    private byte[] paramsDataBytes;

    @ApiModelProperty("发数据")
    private byte[] sendBytes;
}
