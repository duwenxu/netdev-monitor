package com.xy.netdev.frame.serivce.impl.device;

import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.bo.DataBodyPara;
import com.xy.netdev.frame.entity.SocketEntity;
import io.netty.buffer.ByteBuf;
import org.mockito.internal.util.collections.Sets;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.xy.netdev.common.util.ByteUtils.bytesToNum;

/**
 * 2.4米卫通天线控制
 * @author cc
 */
@Service
public class AntennaControlImpl extends AbsDeviceSocketHandler<DataBodyPara> {

    @Override
    public String deviceMark() {
        return null;
    }

    @Override
    public Set<String> queryMark() {
        return Sets.newSet("10");
    }

    @Override
    public Set<String> queryResultMark() {
        return Sets.newSet("10");
    }

    @Override
    public <T extends SocketEntity, R extends DataBodyPara> R unpack(T t) {
        byte[] originalReceiveBytes = t.getOriginalReceiveBytes();
        //信息长度
        Byte length = bytesToNum(originalReceiveBytes, 1, 1, ByteBuf::readByte);
        //数据体
        byte[] paramData = ByteUtils.byteArrayCopy(originalReceiveBytes, 4, length);
        //数据体解析
        return null;
    }

    @Override
    public <T extends SocketEntity, R extends DataBodyPara> T pack(R r) {
        return null;
    }


}
