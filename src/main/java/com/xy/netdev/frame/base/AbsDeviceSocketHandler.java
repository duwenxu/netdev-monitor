package com.xy.netdev.frame.base;

import com.xy.netdev.frame.bo.DataBodyPara;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.serivce.SocketMutualService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public abstract class AbsDeviceSocketHandler<T extends DataBodyPara> extends DeviceSocketBaseHandler<T> {

    @Lazy
    @Autowired
    private SocketMutualService socketMutualService;

    private volatile String receiveFlag = "";

    private volatile String sendFlag = "";

    @Override
    public String nowSendFlag() {
        return this.sendFlag;
    }

    @Override
    public String nowReceiveFlag() {
        return this.receiveFlag;
    }


    protected void setSendFlag(String sendFlag){
        this.sendFlag = sendFlag;
    }

    protected void setReceiveFlag(String receiveFlag){
        this.receiveFlag = receiveFlag;
    }

    @Override
    public void request(T t) {
        //查询
        if (queryMark().contains(nowReceiveFlag())){
            doQuery();
            return;
        }
        //控制
        if (controlMark().contains(nowReceiveFlag())){
            doControl();
        }
    }


    @Override
    public void response(SocketEntity socketEntity) {
        //查询应答
        if (queryResultMark().contains(nowSendFlag())){
            socketMutualService.callBack(doQueryResult());
            return;
        }
        //控制应答
        if (controlResultMark().contains(nowSendFlag())){
            socketMutualService.callBack(doControlResult());
        }
    }

    /**
     * 中心回调接口
     * @param list 中心数据格式
     */
    public void callBack(List<T>list){

    }


}
