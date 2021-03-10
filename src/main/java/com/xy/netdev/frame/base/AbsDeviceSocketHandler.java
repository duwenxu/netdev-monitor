package com.xy.netdev.frame.base;

import com.xy.netdev.frame.base.service.ProtocolPackService;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.entity.TransportEntity;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.xy.netdev.common.util.ByteUtils.byteToNumber;
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

    /**
     * 回调
     * @param t
     */
    public abstract void callback(T t);

    @Override
    public void socketResponse(SocketEntity socketEntity) {
        this.callback(unpack((R)socketEntity));
    }


    /**
     * 字节数组转中心参数
     * @param list 参数信息
     * @param bytes 原始字节数据
     */
    protected List<FrameParaInfo> byteParamToFrameParaInfo(List<FrameParaInfo> list, byte[] bytes){
        list.forEach(paraInfo -> {
            int offset = paraInfo.getParaStartPoint();
            int byteLen = Integer.parseInt(paraInfo.getParaByteLen());
            String value = byteToNumber(bytes, offset, byteLen).toString();
            paraInfo.setParaVal(value);
        });
        return list;
    }

    public static List<FrameParaInfo> getParamByIp(String ip, String itfCode){
        BaseInfo devInfo = getDevInfo(ip);
        return getInterLinkParaList(devInfo.getDevType(), itfCode);
    }


}
