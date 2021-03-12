package com.xy.netdev.frame.service.impl.head;

import cn.hutool.core.util.NumberUtil;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.entity.device.ModemEntity;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.*;

/**
 * 调制解调器
 */
@Service
@Slf4j
public class ModemImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData>{


    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService) {
        switch (frameRespData.getCmdMark()){
            case "53":
                iParaPrtclAnalysisService.queryParaResponse(frameRespData);
                break;
            case "41":
                iParaPrtclAnalysisService.ctrlParaResponse(frameRespData);
                break;
            default:
                log.warn("设备:{},未知调制解调器类型:{}", frameRespData.getDevNo(), frameRespData.getCmdMark());
                break;
        }
    }


    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
        byte[] bytes = socketEntity.getBytes();
        //数据体长度
        int len = bytesToNum(bytes, 1, 2, ByteBuf::readShort) ;
        //参数
        Byte cmd = bytesToNum(bytes, 5, 1, ByteBuf::readByte);
        String hexCmdmark = Integer.toHexString(cmd);
        //数据体
<<<<<<< HEAD
        byte[] paramBytes = byteArrayCopy(bytes, 6, len);
        frameRespData.setCmdMark(hexCmdmark);
=======
        byte[] paramBytes = byteArrayCopy(bytes, 6, len - 4);
        frameRespData.setCmdMark(Integer.toHexString(cmd));
>>>>>>> origin/dev
        frameRespData.setParamBytes(paramBytes);
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        byte[] paramBytes = frameReqData.getParamBytes();
        int len = paramBytes.length + 8;

        ModemEntity modemEntity = ModemEntity.builder()
                .beginOffset((byte)0x02)
                .num(objectToBytes(len, 2))
                .deviceType((byte)0x65)
                .deviceAddress((byte)0x01)
                .cmd(Byte.valueOf(frameReqData.getCmdMark()))
                .params(paramBytes)
                .check((byte)0)
                .end((byte)0x0A)
                .build();
        byte check = check(modemEntity);
        modemEntity.setCheck(check);
        return pack(modemEntity);
    }

    private byte[] pack(ModemEntity modemEntity){
        List<byte[]> list = new ArrayList<>();
        list.add(new byte[]{modemEntity.getBeginOffset()});
        list.add(modemEntity.getNum());
        list.add(new byte[]{modemEntity.getDeviceType()});
        list.add(new byte[]{modemEntity.getDeviceAddress()});
        list.add(new byte[]{modemEntity.getCmd()});
        if (modemEntity.getParams() != null){
            list.add(modemEntity.getParams());
        }
        list.add(new byte[]{modemEntity.getCheck()});
        list.add(new byte[]{modemEntity.getEnd()});
        return listToBytes(list);
    }


    public byte check(ModemEntity modemEntity){
        return addDiv(
                byteToInt(modemEntity.getNum())
                , byteToInt(modemEntity.getDeviceType())
                , byteToInt(modemEntity.getDeviceType())
                , byteToInt(modemEntity.getDeviceAddress())
                , byteToInt(modemEntity.getCmd())
                , byteToInt(modemEntity.getParams())
                );
    }

    private byte addDiv(int... values){
        double div = NumberUtil.div(Arrays.stream(values).sum(), 256);
        return (byte)Double.valueOf(div).intValue();
    }

}
