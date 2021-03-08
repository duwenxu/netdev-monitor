package com.xy.netdev.frame.service.impl.device;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.bo.DataBodyPara;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.entity.device.AntennaControlEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.assertj.core.internal.Bytes;
import org.mockito.internal.util.collections.Sets;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.xy.netdev.common.util.ByteUtils.bytesToNum;
import static com.xy.netdev.common.util.ByteUtils.listToBytes;

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
