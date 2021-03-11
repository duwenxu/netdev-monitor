package com.xy.netdev.frame.base;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.base.service.ProtocolPackService;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.network.NettyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.xy.netdev.container.BaseInfoContainer.getDevInfo;

/**
 * 设备数据流程处理基类
 * @author cc
 */
@Component
@Slf4j
public abstract class AbsDeviceSocketHandler<Q extends SocketEntity, T extends FrameReqData, R extends FrameRespData>
        extends DeviceSocketBaseHandler<T, R> implements ProtocolPackService<Q, T, R> {

    @Resource
    protected IParaPrtclAnalysisService iParaPrtclAnalysisService;

    @Override
    public void socketRequest(T t, ProtocolRequestEnum requestEnum) {
        switch (requestEnum){
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
                doQuery(t);
                break;
        }
    }

    @Override
    public void doQuery(T t) {
        byte[] bytes = pack(t);
        sendData(t, bytes);
        t.setSendOrignData(HexUtil.encodeHexStr(bytes));
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
        BaseInfo devInfo = getDevInfo(socketEntity.getRemoteAddress());
        FrameRespData frameRespData = new FrameRespData();
        frameRespData.setDevType(devInfo.getDevType());
        frameRespData.setDevNo(devInfo.getDevNo());
        R unpack = unpack((Q) socketEntity, (R) frameRespData);
        frameRespData.setReciveOrignData(HexUtil.encodeHexStr(frameRespData.getParamBytes()));
        this.callback(unpack);
    }

    /**
     * 回调
     * @param r
     */
    public abstract void callback(R r);


    protected void sendData(T t, byte[] bytes) {
        BaseInfo devInfo = BaseInfoContainer.getDevInfoByNo(t.getDevNo());
        int port = Integer.parseInt(devInfo.getDevPort());
        NettyUtil.sendMsg(bytes, port, devInfo.getDevIpAddr(), port, Integer.parseInt(devInfo.getDevNetPtcl()));
    }


}
