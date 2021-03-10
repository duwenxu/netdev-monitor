package com.xy.netdev.frame.base;

import com.xy.netdev.frame.base.service.ProtocolPackService;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.entity.TransportEntity;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.ParaInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.xy.netdev.container.BaseInfoContainer.getDevInfo;
import static com.xy.netdev.container.BaseInfoContainer.getInterLinkParaList;

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
    public void socketResponse(SocketEntity socketEntity) {
        this.callback(unpack((R)socketEntity));
    }

    /**
     * 回调
     * @param t
     */
    public abstract void callback(T t);


    public static List<FrameParaInfo> getParamByIp(String ip, String itfCode){
        BaseInfo devInfo = getDevInfo(ip);
        List<FrameParaInfo> interLinkParaList = getInterLinkParaList(devInfo.getDevType(), itfCode);
        return null;
    }


}
