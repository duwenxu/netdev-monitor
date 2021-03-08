package com.xy.netdev.frame.base;

import com.xy.netdev.frame.base.service.ProtocolPackService;
import com.xy.netdev.frame.bo.DataBodyPara;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.entity.TransportEntity;
import com.xy.netdev.frame.service.SocketMutualService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 设备数据流程处理基类
 * @author cc
 */
@Component
public abstract class AbsDeviceSocketHandler<T extends TransportEntity> extends DeviceSocketBaseHandler<T> implements ProtocolPackService {

    @Lazy
    @Autowired
    private SocketMutualService socketMutualService;

    private volatile String nowReceiveFlag = "";

    private volatile String nowSendFlag = "";

    @Override
    public String nowSendFlag() {
        return this.nowSendFlag;
    }

    @Override
    public String nowReceiveFlag() {
        return this.nowReceiveFlag;
    }


    protected void setNowSendFlag(String sendFlag){
        this.nowSendFlag = sendFlag;
    }

    protected void setNowReceiveFlag(String receiveFlag){
        this.nowReceiveFlag = receiveFlag;
    }

    @Override
    public void request(T t) {
        //查询
        if (queryMark().contains(nowReceiveFlag())){
            doQuery(t);
            return;
        }
        //控制
        if (controlMark().contains(nowReceiveFlag())){
            doControl(t);
        }
    }

    @Override
    public void response(SocketEntity socketEntity) {
        //查询应答
        if (queryResultMark().contains(nowSendFlag())){
            socketMutualService.callback(doQueryResult());
            return;
        }
        //控制应答
        if (controlResultMark().contains(nowSendFlag())){
            socketMutualService.callback(doControlResult());
        }
    }


    @Override
    public <T1 extends TransportEntity> void doQuery(T1 t1) {
        super.doQuery(t1);
    }
}
