package com.xy.netdev.rpt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @Description: Comtech特殊命令枚举
 * @Date 15:13 2021/5/21
 * @Author duwx
 */
@AllArgsConstructor
@Getter
@ToString
public enum ComtechSpeComEnum {

    /**参考频率微调字*/
    PBM("PBM?", "PBM",new byte[]{0x50,0x42,0x4D}),
    /**LED状态模式*/
    PBW("PBW?", "PBW",new byte[]{0x50,0x42,0x57});

    private final String reqCommand;
    private final String respCommand;
    private final byte[] bytes;
}
