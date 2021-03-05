package com.xy.netdev.socket;


public enum SocketTypeEnum  {

    UDP(0, "UDP"),
//    TCP_SERVER(1, "tcp_server"),
    TCP_CLIENT(2, "TCP客户端"),
    MULTICAST(3, "组播"),


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