package com.xy.netdev.sendrecv.entity;

import cn.hutool.core.clone.CloneRuntimeException;
import cn.hutool.core.clone.CloneSupport;
import cn.hutool.core.clone.Cloneable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * socket数据传输类
 * @author cc
 */
@Setter
@Getter
public class SocketEntity extends CloneSupport<SocketEntity> {
    @ApiModelProperty("本地端口")
    private Integer localPort;

    @ApiModelProperty("远程端口")
    private Integer remotePort;

    @ApiModelProperty("远程地址")
    private String remoteAddress;

    @ApiModelProperty("数据体")
    private byte[] bytes;


  public static class SocketEntityFactory{
    private static final SocketEntity ENTITY = new SocketEntity();
    public static SocketEntity cloneable(){
        return ENTITY.clone();
    }
   }
}
