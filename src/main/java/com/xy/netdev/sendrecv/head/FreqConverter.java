package com.xy.netdev.sendrecv.head;

import cn.hutool.core.util.HexUtil;
import com.google.common.primitives.Bytes;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.sendrecv.entity.device.FreqConverterEntity;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.xy.netdev.common.constant.SysConfigConstant.*;
import static com.xy.netdev.common.util.ByteUtils.*;


/**
 * 6941公司Ku/L下变频器
 *
 * @author duwenxu
 * @create 2021-04-13 14:10
 */
@Component
@Slf4j
public class FreqConverter extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {

    /**
     * 请求信息帧尾
     */
    private static final String REQUEST_END = "AAAAAAAA";
    /**
     * 响应信息帧尾
     */
    private static final String RESPONSE_END = "7B7B7B7B";

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
        log.info("接收到6914变频器查询响应帧：设备类型：[{}]，响应帧内容：[{}]",frameRespData.getDevType(),HexUtil.encodeHexStr(bytes));
        //转小端
//        Bytes.reverse(bytes);
        //帧长
        Long length = bytesToNum(bytes, 4, 4, ByteBuf::readUnsignedInt);
        //帧头（10）+帧尾（4）+校验字（4）
        if (bytes.length < 18) {
            log.warn("下变频器响应数据帧异常, 响应数据长度错误, 数据体长度:[{}], 数据体:[{}]", bytes.length, HexUtil.encodeHexStr(bytes));
            return frameRespData;
        }
        //帧头
        String hexFrameHead = HexUtil.encodeHexStr(Objects.requireNonNull(byteArrayCopy(bytes, 0, 4))).toUpperCase();
        String hexFrameEnd = HexUtil.encodeHexStr(Objects.requireNonNull(byteArrayCopy(bytes, length.intValue()-4, 4))).toUpperCase();
        //帧ID(接口cmk)
        String messageId = HexUtil.encodeHexStr(Objects.requireNonNull(byteArrayCopy(bytes, 8, 2))).toUpperCase();
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterface(frameRespData.getDevType(), messageId);
        String responseHead = StringUtils.isBlank(prtclFormat.getFmtSckey()) ? prtclFormat.getFmtCckey() : prtclFormat.getFmtSckey();
        if (!responseHead.equals(hexFrameHead)||!RESPONSE_END.equals(hexFrameEnd)) {
            log.error("下变频器接收数据帧帧头或帧尾错误：帧头内容：[{}]，帧尾内容：[{}], 数据体：[{}]", hexFrameHead,hexFrameEnd, HexUtil.encodeHexStr(bytes));
        }
        byte[] context = byteArrayCopy(bytes, 10, length.intValue() - 14);

        frameRespData.setCmdMark(messageId);
        frameRespData.setParamBytes(context);
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        byte[] paramBytes = frameReqData.getParamBytes();
        String cmdMark = frameReqData.getCmdMark();
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterface(frameReqData.getDevType(), cmdMark);
        String requestHead = StringUtils.isBlank(prtclFormat.getFmtSkey()) ? prtclFormat.getFmtCkey() : prtclFormat.getFmtSkey();
        //内容长度 + 其它固定帧字节长度
        int msgLen = 18;
        if (paramBytes!=null&& paramBytes.length!=0){
            msgLen = msgLen+ paramBytes.length;
        }
        FreqConverterEntity entity = FreqConverterEntity.builder()
                .head(HexUtil.decodeHex(requestHead))
                .msgLen(objToBytes(msgLen, 4))
                .msgId(HexUtil.decodeHex(cmdMark))
                .end(HexUtil.decodeHex(REQUEST_END))
                .build();
        if (paramBytes!=null){
            entity.setParams(paramBytes);
        }
        //累加校验和
        byte[] checkSum = addGetBottom(entity);
        entity.setCheck(checkSum);
        byte[] pack = pack(entity);
        //反转小端
//        Bytes.reverse(pack);
        if (OPREATE_QUERY.equals(frameReqData.getOperType())){
            log.info("6914变频器发送查询帧：[{}]",HexUtil.encodeHexStr(pack));
        }else {
            log.info("6914变频器发送控制帧：[{}]",HexUtil.encodeHexStr(pack));
        }
        return pack;
    }

    private byte[] addGetBottom(FreqConverterEntity entity) {
        List<byte[]> list = new ArrayList<>();
        list.add(entity.getMsgLen());
        list.add(entity.getMsgId());
        if (entity.getParams() != null) {
            list.add(entity.getParams());
        }
        byte[] bytes = listToBytes(list);
        return doAddGetBottom(bytes, 0, bytes.length);
    }

    /**
     * 累加字节数组取低位
     *
     * @param bytes  原始数组
     * @param offset 起始位
     * @param len    长度
     * @return 低位
     */
    public static byte[] doAddGetBottom(byte[] bytes, int offset, int len) {
        byte[] arrayCopy = byteArrayCopy(bytes, offset, len);
        int sum = 0;
        for (byte b : Objects.requireNonNull(arrayCopy)) {
            sum += (b & 0xFF);
        }
        return objToBytes(sum, 4);
    }

    private byte[] pack(FreqConverterEntity entity) {
        List<byte[]> list = new ArrayList<>();
        list.add(entity.getHead());
        list.add(entity.getMsgLen());
        list.add(entity.getMsgId());
        if (entity.getParams() != null) {
            list.add(entity.getParams());
        }
        list.add(entity.getCheck());
        list.add(entity.getEnd());
        return listToBytes(list);
    }
}
