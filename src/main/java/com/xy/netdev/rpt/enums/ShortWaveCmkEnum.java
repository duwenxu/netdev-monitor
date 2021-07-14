package com.xy.netdev.rpt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/***
 * @Description 400W短波选频率设备 命令字枚举
 * @Date 17:15 2021/7/12
 * @author duwx
 **/
@AllArgsConstructor
@Getter
@ToString
public enum ShortWaveCmkEnum {

    QUERY_CHANNEL("10","11","12","查询信道状态"),
    START_CHANNEL("20","21","22","建链"),
    END_CHANNEL("30","31","32","拆链"),
    SEND_DATA("40","41","42","发送数据");

    private final String reqCmk;
    private final String tempResCmk;
    private final String respCmk;
    private final String desc;
}
