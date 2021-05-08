package com.xy.netdev.sendrecv.head;

import cn.hutool.core.util.HexUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.sendrecv.entity.device.KaPowerAmpEntity;
import com.xy.netdev.sendrecv.entity.device.PowerAmpEntity;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.xy.netdev.common.constant.SysConfigConstant.*;
import static com.xy.netdev.common.util.ByteUtils.*;

/**
 * Ka 100W发射机
 *
 * @author duwenxu
 * @create 2021-04-30 9:25
 */
@Service
@Slf4j
public class KaPowerAmpImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {
    /**主机请求帧头*/
    private static final String REQUEST_HEAD = "7B";
    /**从机响应帧头*/
    private static final String RESPONSE_HEAD = "7F";
    /**请求响应标识*/
    public static final List<String> REQUEST_CMD = Arrays.asList("20","21","31","32");
    /**控制响应标识*/
    private static final String RESPONSE_CMD = "10";

    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService, IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService, ICtrlInterPrtclAnalysisService ctrlInterPrtclAnalysisService) {
        String operType = frameRespData.getOperType();
        switch (operType) {
            case OPREATE_QUERY_RESP:
                iQueryInterPrtclAnalysisService.queryParaResponse(frameRespData);
                break;
            case OPREATE_CONTROL_RESP:
                iParaPrtclAnalysisService.ctrlParaResponse(frameRespData);
                break;
            default:
                log.warn("设备:{},未知的参数类型:{}", frameRespData.getDevNo(), frameRespData.getCmdMark());
                break;
        }
    }

    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
        byte[] bytes = socketEntity.getBytes();
        log.info("Ka100W发射机收到响应帧：[{}]", HexUtil.encodeHexStr(bytes));
        if (bytes.length <= 6) {
            log.warn("Ka100W发射机响应数据长度错误, 未能正确解析, 数据体长度:{}, 数据体:{}", bytes.length, HexUtil.encodeHexStr(bytes));
            return frameRespData;
        }
        //帧头标识
        String frameHead = HexUtil.encodeHexStr(Objects.requireNonNull(byteArrayCopy(bytes, 0, 1))).toUpperCase();
        if (!RESPONSE_HEAD.equals(frameHead)) {
            log.warn("Ka100W发射机响应数据帧头错误：协议帧头：[{}]，当前帧头：[{}]", RESPONSE_HEAD, frameHead);
            return frameRespData;
        }
        //按协议中指定的位置获取数据体长度
        int len = bytesToNum(bytes, 1, 1, ByteBuf::readUnsignedByte);
        //命令码
        Short cmd = bytesToNum(bytes, 3, 1, ByteBuf::readUnsignedByte);
        String hexCmd = HexUtil.toHex(cmd);
        //数据体(总长度-其它固定字节长度)
        byte[] paramBytes = byteArrayCopy(bytes, 4, len - 6);
        if (REQUEST_CMD.contains(hexCmd)) {
            frameRespData.setOperType(OPREATE_CONTROL_RESP);
        } else {
            frameRespData.setOperType(OPREATE_QUERY_RESP);
        }
        frameRespData.setCmdMark(hexCmd);
        frameRespData.setParamBytes(paramBytes);
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        //参数数据
        byte[] paramBytes = frameReqData.getParamBytes();
        //数据长度 命令字固定1字节，存在参数时增加参数体长度
        int dataLength = 6;
        if (paramBytes != null) {
            dataLength += paramBytes.length;
        }
        String operType = frameReqData.getOperType();
        String keyWord;
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(frameReqData.getDevType(), frameReqData.getCmdMark());
        if (prtclFormat.getFmtId() == null){
            throw new BaseException("设备类型为"+frameReqData.getDevType()+"，参数命令为"+frameReqData.getCmdMark()+"协议格式获取失败...");
        }
        if (OPREATE_QUERY.equals(operType)){
            keyWord = prtclFormat.getFmtSkey();
        }else {
            keyWord = prtclFormat.getFmtCkey();
        }
        KaPowerAmpEntity ampEntity = KaPowerAmpEntity.builder()
                .head((byte) 0x7B)
                .msgLen((byte) dataLength)
                //TODO 地址字段待确定
                .sad((byte) 0x01)
                .cmd((byte)Integer.parseInt(keyWord,16))
                .data(paramBytes)
                .end((byte) 0x7D)
                .build();
        //除帧头帧尾外其它字段异或 校验和
        byte checkSum = xorCheck(ampEntity);
        ampEntity.setCheck(checkSum);
        byte[] pack = pack(ampEntity);

        if (OPREATE_QUERY.equals(frameReqData.getOperType())){
            log.info("Ka100W发射机发送查询帧：[{}]",HexUtil.encodeHexStr(pack));
        }else {
            log.info("Ka100W发射机发送控制帧：[{}]",HexUtil.encodeHexStr(pack));
        }
        return pack;
    }

    /**
     * 异或获取校验和
     *
     * @param ampEntity
     * @return
     */
    private byte xorCheck(KaPowerAmpEntity ampEntity) {
        byte[] bytes = new byte[]{ampEntity.getMsgLen(), ampEntity.getSad(), ampEntity.getCmd()};
        if (ampEntity.getData()!= null){
            bytes = bytesMerge(bytes, ampEntity.getData());
        }
        byte temp = bytes[0];
        for (int i = 1; i < bytes.length; i++) {
            temp ^= bytes[i];
        }
        return temp;
    }

    private byte[] pack(KaPowerAmpEntity ampEntity) {
        List<byte[]> list = new ArrayList<>();
        list.add(new byte[]{ampEntity.getHead()});
        list.add(new byte[]{ampEntity.getMsgLen()});
        list.add(new byte[]{ampEntity.getSad()});
        list.add(new byte[]{ampEntity.getCmd()});
        if (ampEntity.getData() != null){
            list.add(ampEntity.getData());
        }
        list.add(new byte[]{ampEntity.getCheck()});
        list.add(new byte[]{ampEntity.getEnd()});
        return listToBytes(list);
    }
}
