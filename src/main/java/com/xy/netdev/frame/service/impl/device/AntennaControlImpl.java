package com.xy.netdev.frame.service.impl.device;

import cn.hutool.core.util.ArrayUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.entity.TransportEntity;
import com.xy.netdev.frame.entity.device.AntennaControlEntity;
import io.netty.buffer.ByteBuf;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.bytesToNum;
import static com.xy.netdev.common.util.ByteUtils.listToBytes;
import static com.xy.netdev.container.BaseInfoContainer.getDevInfo;

/**
 * 2.4米卫通天线控制
 * @author cc
 */
@Service
public class AntennaControlImpl extends AbsDeviceSocketHandler<SocketEntity, TransportEntity> {


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
        transportEntity.setDevInfo(getDevInfo(socketEntity.getRemoteAddress()));
        //数据体解析
        return transportEntity;
    }

    @Override
    public byte[] pack(TransportEntity transportEntity) {
        //参数数据
        byte[] paramBytes = transportEntity.getParamBytes();
        //数据长度
        int dataLength = paramBytes.length + 6;

        AntennaControlEntity antennaControlEntity = AntennaControlEntity.builder()
                .stx((byte) 0x7B)
                .lc((byte) dataLength)
                .sad((byte) 0)
                .cmd(Byte.valueOf(transportEntity.getParamMark()))
                .data(transportEntity.getParamBytes())
                .vs((byte) 0)
                .etx((byte) 0x7D)
                .build();
        byte vs = xor(antennaControlEntity);
        antennaControlEntity.setVs(vs);
        return pack(antennaControlEntity);
    }


    @Override
    public void callback(TransportEntity transportEntity) {
        iParaPrtclAnalysisService.ctrlParaResponse(transportEntity);
    }

    private byte xor( AntennaControlEntity entity){
        byte[] bytes = {entity.getLc(), entity.getSad(), entity.getCmd()};
        byte[] xorBytes = ArrayUtil.addAll(bytes, entity.getData());
        return ByteUtils.xor(xorBytes, 0, xorBytes.length);
    }

    private byte[] pack(AntennaControlEntity antennaControlEntity){
    List<byte[]> list = new ArrayList<>();
    list.add(new byte[]{antennaControlEntity.getStx()});
    list.add(new byte[]{antennaControlEntity.getLc()});
    list.add(new byte[]{antennaControlEntity.getSad()});
    list.add(new byte[]{antennaControlEntity.getCmd()});
    list.add(antennaControlEntity.getData());
    list.add(new byte[]{antennaControlEntity.getVs()});
    list.add(new byte[]{antennaControlEntity.getEtx()});
    return listToBytes(list);
}

}
