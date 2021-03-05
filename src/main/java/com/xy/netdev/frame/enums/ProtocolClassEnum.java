package com.xy.netdev.frame.enums;

import lombok.Getter;

public enum ProtocolClassEnum {

    /**
     * 4米卫通天线控制接口协议
     */
    ANTENNA_CONTROL("AntennaControlImpl", "2.4米卫通天线控制接口协议");

    ProtocolClassEnum(String key, String value){
        this.key = key;
        this.value = value;
    }

    @Getter
    private String key;

    @Getter
    private String value;
}
