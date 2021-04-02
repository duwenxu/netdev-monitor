package com.xy.netdev.sendrecv.entity.device;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * SCMM-2300调制解调器协议头对象
 * @author duwenxu
 * @create 2021-03-30 11:12
 */
@Getter
@Setter
@Builder
public class ModemScmmEntity {

    @ApiModelProperty("起始字节")
    private Byte beginOffset;

    @ApiModelProperty("长度")
    private Byte length;

    @ApiModelProperty("命令字编码")
    private Byte cmd;

    @ApiModelProperty("设置单元")
    private Byte unit;

    @ApiModelProperty("参数信息(关键字+信息体)")
    private byte[] value;

    @ApiModelProperty("校验字节")
    private Byte check;

    @ApiModelProperty("结束符")
    private Byte end;
}
