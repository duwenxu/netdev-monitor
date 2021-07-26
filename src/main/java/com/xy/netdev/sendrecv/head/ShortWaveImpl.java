package com.xy.netdev.sendrecv.head;

import cn.hutool.core.util.HexUtil;
import com.google.common.primitives.Longs;
import com.xy.common.exception.BaseException;
import com.xy.netdev.common.util.crc.Crc16;
import com.xy.netdev.common.util.crc.CrcCalculator;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.shortwave.ShortWaveInterCtrlServiceImpl;
import com.xy.netdev.frame.service.shortwave.ShortWaveInterPrtcServiceImpl;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.sendrecv.entity.device.ShortWaveEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.xy.netdev.common.constant.SysConfigConstant.*;
import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;
import static com.xy.netdev.common.util.ByteUtils.listToBytes;

/**
 * 750-400W短波设备 头解析协议
 */
@Slf4j
@Component
public class ShortWaveImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {

    /**请求及控制帧 统一帧头字节*/
    private static final byte[] FRAME_HEAD = new byte[]{0x55,(byte) 0xAA};

    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService, IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService, ICtrlInterPrtclAnalysisService ctrlInterPrtclAnalysisService) {
        String operType = frameRespData.getOperType();
        switch (operType) {
            case OPREATE_QUERY_RESP:
                iQueryInterPrtclAnalysisService.queryParaResponse(frameRespData);
                break;
            case OPREATE_CONTROL_RESP:
                ctrlInterPrtclAnalysisService.ctrlParaResponse(frameRespData);
                break;
            default:
                log.warn("设备:{},未知的参数类型:{}", frameRespData.getDevNo(), frameRespData.getCmdMark());
                break;
        }
    }

    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
        byte[] bytes = socketEntity.getBytes();
        log.info("接收到750-400W短波设备响应帧：设备类型：[{}]，响应帧内容：[{}]",frameRespData.getDevType(), HexUtil.encodeHexStr(bytes).toUpperCase());
        //响应固定字节 报头2+命令字1+序号4+CRC校验2
        if (bytes.length < 9) {
            log.warn("750-400W短波设备响应数据帧异常, 响应数据长度错误, 数据体长度:[{}], 数据体:[{}]", bytes.length, HexUtil.encodeHexStr(bytes));
            return frameRespData;
        }
        //命令字(cmk)
        String hexCmk = HexUtil.encodeHexStr(Objects.requireNonNull(byteArrayCopy(bytes, 2, 1))).toUpperCase();
        //序号
        String hexSerialNum = HexUtil.encodeHexStr(Objects.requireNonNull(byteArrayCopy(bytes, bytes.length-6, 4))).toUpperCase();
        //参数结果数据体(包含序号)
        byte[] context = byteArrayCopy(bytes, 3, bytes.length-5);

        frameRespData.setCmdMark(hexCmk);
        frameRespData.setParamBytes(context);
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        //参数数据
        byte[] paramBytes = frameReqData.getParamBytes();
        String cmdMark = frameReqData.getCmdMark();

        String operType = frameReqData.getOperType();
        String keyWord;
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(frameReqData.getDevType(), cmdMark);
        if (prtclFormat.getFmtId() == null){
            throw new BaseException("设备类型为"+frameReqData.getDevType()+"，参数命令为"+ cmdMark +"协议格式获取失败...");
        }
        if (OPREATE_QUERY.equals(operType)){
            keyWord = prtclFormat.getFmtSkey();
        }else {
            keyWord = prtclFormat.getFmtCkey();
        }
        if (StringUtils.isBlank(keyWord)){
            keyWord = cmdMark;
        }

        byte[] serialNumBytes = new byte[]{};
        switch (cmdMark){
            case "10":
                serialNumBytes = ShortWaveInterPrtcServiceImpl.QUERY_CHANNEL_STATUS;
                break;
            case "20":
                serialNumBytes = ShortWaveInterCtrlServiceImpl.START_CHANNEL;
                break;
            case "30":
                serialNumBytes = ShortWaveInterPrtcServiceImpl.END_CHANNEL;
                break;
            case "40":
                serialNumBytes = ShortWaveInterCtrlServiceImpl.SEND_DATA;
                break;
            default:
                log.error("400W短波当前发送命令字错误，当前命令字为：[{}]",cmdMark);
        }

        ShortWaveEntity waveEntity = ShortWaveEntity.builder()
                .frameHead(FRAME_HEAD)
                .cmk((byte) Integer.parseInt(keyWord,16))
                .data(paramBytes)
                .serialNum(serialNumBytes)
                .build();
        //CRC校验和
        byte[] checkSum = crc16Check(waveEntity);
        waveEntity.setCrc(checkSum);
        byte[] pack = pack(waveEntity);

        if (OPREATE_QUERY.equals(frameReqData.getOperType())){
            log.info("400W短波设备发送查询帧：[{}]",HexUtil.encodeHexStr(pack));
        }else {
            log.info("400W短波设备发送控制帧：[{}]",HexUtil.encodeHexStr(pack));
        }
        return pack;
    }

    private byte[] pack(ShortWaveEntity entity) {
        List<byte[]> list = new ArrayList<>();
        list.add(entity.getFrameHead());
        list.add(new byte[]{entity.getCmk()});
        if (entity.getData() != null) {
            list.add(entity.getData());
        }
        list.add(entity.getSerialNum());
        list.add(entity.getCrc());
        return listToBytes(list);
    }

    /**
     * CRC校验码
     * @param waveEntity 校验实体类
     * @return CRC校验码
     */
    private byte[] crc16Check(ShortWaveEntity waveEntity) {
        byte[] bytes = packForCrc(waveEntity);
        CrcCalculator crcCalculator = new CrcCalculator(Crc16.Crc16Xmodem);
        long crc16 = crcCalculator.Calc(bytes, 0, bytes.length);
        byte[] crcBytes = Longs.toByteArray(crc16);
        return byteArrayCopy(crcBytes, crcBytes.length - 2, 2);
    }

    private byte[] packForCrc(ShortWaveEntity entity) {
        List<byte[]> list = new ArrayList<>();
        list.add(entity.getFrameHead());
        list.add(new byte[]{entity.getCmk()});
        if (entity.getData() != null) {
            list.add(entity.getData());
        }
        list.add(entity.getSerialNum());
        return listToBytes(list);
    }
}
