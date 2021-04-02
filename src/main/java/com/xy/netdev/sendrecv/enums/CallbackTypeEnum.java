package com.xy.netdev.sendrecv.enums;

import lombok.Getter;

public enum CallbackTypeEnum {
    DEFAULT("", "");
    ;
    CallbackTypeEnum(String key, String value){
        this.key = key;
        this.value = value;
    }

    @Getter
    private String key;

    @Getter
    private String value;

}
