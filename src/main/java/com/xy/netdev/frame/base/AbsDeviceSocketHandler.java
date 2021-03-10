package com.xy.netdev.frame.base;

import com.xy.netdev.frame.base.service.ProtocolPackService;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.entity.TransportEntity;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.network.NettyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设备数据流程处理基类
 * @author cc
 */
@Component
@Slf4j
public abstract class AbsDeviceSocketHandler<R extends SocketEntity, T extends TransportEntity> extends DeviceSocketBaseHandler<T> implements ProtocolPackService<R, T> {

    @Override
    public void socketRequest(T t, ProtocolRequestEnum requestEnum) {
        switch (requestEnum){
            case QUERY:
                doQuery(t);
                break;
            case CONTROL:
                doControl(t);
                break;
            case QUERY_RESULT:
                doQueryResult(t);
                break;
            case CONTROL_RESULT:
                doControlResult(t);
                break;
            default:
                log.warn("未知请求类型");
                break;
        }
    }

    @Override
    public void doQuery(T t) {
        byte[] bytes = pack(t);
        BaseInfo devInfo = t.getDevInfo();
        int port = Integer.parseInt(devInfo.getDevPort());
        NettyUtil.sendMsg(bytes, port, devInfo.getDevIpAddr(), port, 0);
    }

    @Override
    public void doControl(T t) {
        this.doQuery(t);
    }

    @Override
    public void doQueryResult(T t) {
        this.doQuery(t);
    }

    @Override
    public void doControlResult(T t) {
        this.doQuery(t);
    }

    @Override
    public void socketResponse(SocketEntity socketEntity) {
        this.callback(unpack((R)socketEntity));
    }

    /**
     * 回调
     * @param t
     */
    public abstract void callback(T t);

}
