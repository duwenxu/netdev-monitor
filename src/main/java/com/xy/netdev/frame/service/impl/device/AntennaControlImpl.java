package com.xy.netdev.frame.service.impl.device;

import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.entity.TransportEntity;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import io.netty.buffer.ByteBuf;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.xy.netdev.common.util.ByteUtils.bytesToNum;
import static com.xy.netdev.common.util.ByteUtils.objectToByte;
import static com.xy.netdev.frame.entity.TransportEntity.setList;

/**
 * 2.4米卫通天线控制
 * @author cc
 */
@Service
public class AntennaControlImpl extends AbsDeviceSocketHandler<TransportEntity> {

    @Override
    public String deviceMark() {
        return null;
    }


    @Override
    public <T extends SocketEntity, R extends TransportEntity> R unpack(T t) {
        byte[] originalReceiveBytes = t.getBytes();
        //数据体长度
        Byte length = bytesToNum(originalReceiveBytes, 1, 1, ByteBuf::readByte);
        //命令
        Byte cmd = bytesToNum(originalReceiveBytes, 3, 1, ByteBuf::readByte);
        //数据体
        byte[] paramData = ByteUtils.byteArrayCopy(originalReceiveBytes, 4, length);
        //数据体解析
        List<FrameParaInfo> paraInfoList = getParamByIp(t.getRemoteAddress(), cmd.toString());
        byteParamToFrameParaInfo(paraInfoList, paramData);
        return setList(paraInfoList);
    }

    @Override
    public <T extends TransportEntity> T pack(T t) {
        List<FrameParaInfo> dataBodyParas = t.getDataBodyParas();
        List<byte[]> list = dataBodyParas.stream()
                .map(paraInfo -> objectToByte(paraInfo.getParaVal(), paraInfo.getParaStartPoint()))
                .collect(Collectors.toList());
//        dataBodyParas.get(0).get
        //参数数据
        byte[] paramByte = ByteUtils.listToBytes(list);
        //数据长度
        int dataLength = paramByte.length;
        return null;
    }


}
