package com.xy.netdev.rpt.enums;

import com.google.common.base.Ascii;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
*
* @Description: 特殊AscII码枚举
* @Date 16:53 2021/5/20
* @Author duwx
*/
@AllArgsConstructor
@Getter
@ToString
public enum AsciiEnum {

    /**消息起始字节*/
    STX("STX",Ascii.STX),
    /**消息结束字节*/
    EXT("EXT",Ascii.ETX),
    /**响应接收字节*/
    ACK("ACK",Ascii.ACK),
    /**响应拒绝字节*/
    NAK("NAK",Ascii.NAK);

    private final String name;
    private final byte code;
}
