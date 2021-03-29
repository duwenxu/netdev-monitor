package com.xy.netdev.sendrecv.head;

import cn.hutool.core.util.ArrayUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.sendrecv.entity.device.AntennaControlEntity;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.transit.IDataReciveService;
import io.netty.buffer.ByteBuf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.bytesToNum;
import static com.xy.netdev.common.util.ByteUtils.listToBytes;

/**
 * 40w功放
 * @author cc
 */
@Service
public class AntennaControlImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {

    @Autowired
    IDataReciveService dataReciveService;

    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService,
                         IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService) {
        if (iParaPrtclAnalysisService != null){
            iParaPrtclAnalysisService.ctrlParaResponse(frameRespData);
            dataReciveService.paraCtrRecive(frameRespData);
            return;
        }
        iQueryInterPrtclAnalysisService.queryParaResponse(frameRespData);
        dataReciveService.paraQueryRecive(frameRespData);
    }

    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
        byte[] originalReceiveBytes = socketEntity.getBytes();
        //数据体长度
        Byte length = bytesToNum(originalReceiveBytes, 1, 1, ByteBuf::readByte);
        //命令
        Byte cmd = bytesToNum(originalReceiveBytes, 3, 1, ByteBuf::readByte);

        Byte respCode = bytesToNum(originalReceiveBytes, 4, 1, ByteBuf::readByte);
        //数据体
        byte[] paramData = ByteUtils.byteArrayCopy(originalReceiveBytes, 4, length  - 6);
        frameRespData.setCmdMark(Integer.toHexString(cmd));
        frameRespData.setParamBytes(paramData);
        frameRespData.setRespCode(respCode.toString());
        bytesToNum(originalReceiveBytes, 4, 1, ByteBuf::readByte);
        //数据体解析
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        //参数数据
        byte[] paramBytes = frameReqData.getParamBytes();
        //数据长度,初始长度为6
        int dataLength = 6;
        if (paramBytes != null){
            dataLength += paramBytes.length;
        }
        AntennaControlEntity antennaControlEntity = AntennaControlEntity.builder()
                .stx((byte) 0x7B)
                .lc((byte) dataLength)
                .sad((byte) 0)
                .cmd(Byte.valueOf(frameReqData.getCmdMark(), 16))
                .data(paramBytes)
                .vs((byte) 0)
                .etx((byte) 0x7D)
                .build();
        byte vs = xor(antennaControlEntity);
        antennaControlEntity.setVs(vs);
        return pack(antennaControlEntity);
    }


    private byte xor(AntennaControlEntity entity){
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
        if (antennaControlEntity.getData() != null){
            list.add(antennaControlEntity.getData());
        }
        list.add(new byte[]{antennaControlEntity.getVs()});
        list.add(new byte[]{antennaControlEntity.getEtx()});
        return listToBytes(list);
    }

}