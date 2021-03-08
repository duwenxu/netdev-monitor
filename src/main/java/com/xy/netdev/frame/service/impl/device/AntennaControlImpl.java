package com.xy.netdev.frame.service.impl.device;

import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.entity.TransportEntity;
import com.xy.netdev.frame.entity.device.AntennaControlEntity;
import com.xy.netdev.monitor.entity.BaseInfo;
import io.netty.buffer.ByteBuf;
import org.mockito.internal.util.collections.Sets;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.xy.netdev.common.util.ByteUtils.bytesToNum;
import static com.xy.netdev.container.BaseInfoContainer.getDevInfo;

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
    public Set<String> queryMark() {
        return Sets.newSet("7B");
    }

    @Override
    public Set<String> queryResultMark() {
        return Sets.newSet("7F");
    }

    @Override
    public <T extends SocketEntity, R extends TransportEntity> R unpack(T t) {
        byte[] originalReceiveBytes = t.getOriginalReceiveBytes();
        //信息长度
        Byte length = bytesToNum(originalReceiveBytes, 1, 1, ByteBuf::readByte);
        //数据体
        byte[] paramData = ByteUtils.byteArrayCopy(originalReceiveBytes, 4, length);
        //数据体解析
        return null;
    }

    @Override
    public <T extends SocketEntity, R extends TransportEntity> T pack(R r) {
        return null;
    }




//
//    private byte[] pack(AntennaControlEntity antennaControlEntity){
//        List<byte[]> list = new ArrayList<>();
//        list.add(new byte[]{antennaControlEntity.getStx()});
//        list.add(new byte[]{antennaControlEntity.getLc()});
//        list.add(new byte[]{antennaControlEntity.getSad()});
//        list.add(new byte[]{antennaControlEntity.getCmd()});
//        list.add(antennaControlEntity.getData());
//        list.add(new byte[]{antennaControlEntity.getVs()});
//        list.add(new byte[]{antennaControlEntity.getEtx()});
//        return listToBytes(list);
//    }

}
