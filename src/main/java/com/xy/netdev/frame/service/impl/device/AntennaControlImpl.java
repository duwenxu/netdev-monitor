package com.xy.netdev.frame.service.impl.device;

import com.baomidou.mybatisplus.extension.api.R;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.entity.TransportEntity;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import io.netty.buffer.ByteBuf;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.xy.netdev.common.util.ByteUtils.bytesToNum;
import static com.xy.netdev.common.util.ByteUtils.objectToByte;
import static com.xy.netdev.container.BaseInfoContainer.getDevInfo;
import static com.xy.netdev.frame.entity.TransportEntity.setList;

/**
 * 2.4米卫通天线控制
 * @author cc
 */
@Service
public class AntennaControlImpl extends AbsDeviceSocketHandler<SocketEntity, TransportEntity> {

    @Override
    public String deviceMark() {
        return null;
    }

    @Override
    public TransportEntity unpack(SocketEntity socketEntity) {
        byte[] originalReceiveBytes = socketEntity.getBytes();
        //数据体长度
        Byte length = bytesToNum(originalReceiveBytes, 1, 1, ByteBuf::readByte);
        //命令
        Byte cmd = bytesToNum(originalReceiveBytes, 3, 1, ByteBuf::readByte);
        //数据体
        byte[] paramData = ByteUtils.byteArrayCopy(originalReceiveBytes, 4, length);
        TransportEntity transportEntity = new TransportEntity();
        transportEntity.setParamMark(cmd.toString());
        transportEntity.setParamBytes(paramData);
        transportEntity.setDevInfo( getDevInfo(socketEntity.getRemoteAddress()));
        //数据体解析
        return transportEntity;
    }

    @Override
    public SocketEntity pack(TransportEntity transportEntity) {
        List<FrameParaInfo> dataBodyParas = transportEntity.getDataBodyParas();
        List<byte[]> list = dataBodyParas.stream()
                .map(paraInfo -> objectToByte(paraInfo.getParaVal(), paraInfo.getParaStartPoint()))
                .collect(Collectors.toList());
        //参数数据
        byte[] paramByte = ByteUtils.listToBytes(list);
        //数据长度
        int dataLength = paramByte.length + 6;
        return null;
    }



    @Override
    public void callback(TransportEntity transportEntity) {

    }
}
