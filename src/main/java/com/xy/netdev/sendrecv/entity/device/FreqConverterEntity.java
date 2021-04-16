package com.xy.netdev.sendrecv.entity.device;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description: 6914 变频器器协议头对象
 * @Date 10:26 2021/4/15
 * @Author duwx
 */
@Setter
@Getter
@Builder
public class FreqConverterEntity {

    @ApiModelProperty(value = "帧头", notes = "4byte")
    private byte[] head;

    @ApiModelProperty(value = "消息长度", notes = "4byte")
    private byte[] msgLen;

    @ApiModelProperty(value = "消息ID", notes = "2byte")
    private byte[] msgId;

    @ApiModelProperty(value = "参数")
    private byte[] params;

    @ApiModelProperty(value = "校验码", notes = "4byte")
    private byte[] check;

    @ApiModelProperty(value = "帧尾", notes = "4byte")
    private byte[] end;

}
