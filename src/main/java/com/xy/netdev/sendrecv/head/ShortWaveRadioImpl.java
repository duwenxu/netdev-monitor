package com.xy.netdev.sendrecv.head;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.common.util.crc.Crc16;
import com.xy.netdev.common.util.crc.CrcCalculator;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.sendrecv.entity.device.ShortWaveRadioEntity;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.xy.netdev.common.constant.SysConfigConstant.OPREATE_QUERY;
import static com.xy.netdev.common.constant.SysConfigConstant.OPREATE_QUERY_RESP;
import static com.xy.netdev.common.util.ByteUtils.*;

/**
 * 712厂短波电台
 */
@Component
@Slf4j
public class ShortWaveRadioImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {

    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService, IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService, ICtrlInterPrtclAnalysisService ctrlInterPrtclAnalysisService) {
        if (iParaPrtclAnalysisService!=null){
            iParaPrtclAnalysisService.queryParaResponse(frameRespData);
        }else {
            log.error("712电台参数上报信息帧，匹配解析协议类型错误....接收到的响应参数：[{}]",frameRespData);
        }
    }

    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
        byte[] bytes = socketEntity.getBytes();
        log.info("接收到712电台参数上报信息帧：设备类型：[{}]，响应帧内容：[{}]",frameRespData.getDevType(), HexUtil.encodeHexStr(bytes));
        //类型ID 命令标识字
        String typeCmk = HexUtil.encodeHexStr(Objects.requireNonNull(byteArrayCopy(bytes, 0, 2))).toUpperCase();
        //帧长
        Short length = bytesToNum(bytes, 2, 2, ByteBuf::readShort);
        //类型ID（2）+校验字（2）+信息长度（2）
        if (bytes.length < 6) {
            log.warn("712电台设备上报参数数据帧异常, 响应数据长度错误, 数据体长度:[{}], 数据体:[{}]", bytes.length, HexUtil.encodeHexStr(bytes));
            return frameRespData;
        }
        byte[] context = byteArrayCopy(bytes, 4, length.intValue());

        //由于协议定义无控制响应，所以默认为查询响应
        frameRespData.setOperType(OPREATE_QUERY_RESP);
        frameRespData.setCmdMark(typeCmk);
        frameRespData.setParamBytes(context);
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        byte[] paramBytes = frameReqData.getParamBytes();
        int infoLen = 0;
        if (paramBytes!=null){
            infoLen = paramBytes.length;
        }
        String cmdMark = frameReqData.getCmdMark();
        ShortWaveRadioEntity waveRadioEntity = ShortWaveRadioEntity.builder()
                .typeCmk(HexUtil.decodeHex(cmdMark))
                .infoLen(objToBytes(infoLen, 2))
                .build();
        if (paramBytes!=null){
            waveRadioEntity.setData(paramBytes);
        }
        //CRC校验和
        byte[] crc16Check = crc16Check(waveRadioEntity);
        waveRadioEntity.setCheck(crc16Check);
        byte[] pack = pack(waveRadioEntity);

        if (OPREATE_QUERY.equals(frameReqData.getOperType())){
            log.info("712电台设备发送查询帧：[{}],查询命令类型：[{}]",HexUtil.encodeHexStr(pack),cmdMark);
        }else {
            log.info("712电台设备发送控制帧：[{}],查询命令类型：[{}]",HexUtil.encodeHexStr(pack),cmdMark);
        }
        return pack;
    }

    private byte[] pack(ShortWaveRadioEntity entity) {
        List<byte[]> list = new ArrayList<>();
        list.add(entity.getTypeCmk());
        list.add(entity.getInfoLen());
        if (entity.getData() != null) {
            list.add(entity.getData());
        }
        list.add(entity.getCheck());
        return listToBytes(list);
    }

    /**
     * CRC校验码   CCITT
     * @param waveEntity 校验实体类
     * @return CRC校验码
     */
    private byte[] crc16Check(ShortWaveRadioEntity waveEntity) {
        byte[] bytes = packForCrc(waveEntity);
        CrcCalculator crcCalculator = new CrcCalculator(Crc16.Crc16Modbus);
        long crc16 = crcCalculator.Calc(bytes, 0, bytes.length);
        String crcHexStr = HexUtil.toHex(crc16);
        return objToBytes(crcHexStr,2);
    }

    private byte[] packForCrc(ShortWaveRadioEntity entity) {
        List<byte[]> list = new ArrayList<>();
        list.add(entity.getTypeCmk());
        list.add(entity.getInfoLen());
        if (entity.getData() != null) {
            list.add(entity.getData());
        }
        return listToBytes(list);
    }

    public static void main(String[] args) {
        byte[] bytes = {0x00, 0x01, 0x00, 0x04, 0x01, 0x23,0x45,0x67};
        CrcCalculator crcCalculator = new CrcCalculator(Crc16.Crc16Modbus);
        long crc16 = crcCalculator.Calc(bytes, 0, bytes.length);
        System.out.println(crc16);
        String s = HexUtil.toHex(crc16);
        System.out.println(s);
        byte[] crcBytes = HexUtil.decodeHex(s);
    }
}
