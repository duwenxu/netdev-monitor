package com.xy.netdev.network.enums;


public enum SocketTypeEnum  {

    /**
     * udp
     */
    UDP(0, "UDP"),
    /**
     * Tcp
     */
    TCP_CLIENT(1, "TCP客户端"),
    /**
     * 组播
     */
    MULTICAST(2, "组播")
    ;
    SocketTypeEnum(int type, String typeName){
        this.type = type;
        this.typeName = typeName;
    }

    private final Integer type;


    private final String typeName;


    public String getTypeName() {
        return typeName;
    }

    public Integer getType() {
        return type;
    }
}