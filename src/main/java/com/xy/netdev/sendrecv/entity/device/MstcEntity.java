package com.xy.netdev.sendrecv.entity.device;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author luo
 * @date 2021/4/21
 */

@Setter
@Getter
@Builder
public class MstcEntity {

    @ApiModelProperty("起始字节")
    private byte[] beginOffset;

    @ApiModelProperty("命令类型")
    private byte cmdType;

    @ApiModelProperty("关键字")
    private byte[] keywords;

    @ApiModelProperty("长度")
    private byte[] length;

    @ApiModelProperty("结构体")
    private byte[] params;

    @ApiModelProperty("校验字")
    private byte check;
}
