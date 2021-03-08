package com.xy.netdev.frame.base.service;

import com.xy.netdev.frame.bo.DataBodyPara;
import com.xy.netdev.frame.entity.SocketEntity;

public interface ProtocolPackService {

    <T extends SocketEntity, R extends DataBodyPara> R unpack(T t);

    <T extends SocketEntity, R extends DataBodyPara> T pack(R r);
}
