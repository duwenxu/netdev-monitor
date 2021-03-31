package com.xy.netdev.sendrecv.head;

import cn.hutool.core.util.HexUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.sendrecv.entity.device.ModemScmmEntity;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.common.constant.SysConfigConstant.*;
import static com.xy.netdev.common.util.ByteUtils.*;

/**
 * SCMM-2300调制解调器协议头解析
 *
 * @author duwenxu
 * @create 2021-03-29 17:14
 */
@Service
@Slf4j
public class ModemScmmImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {
    /**
     * 查询/控制响应命令标识
     */
    private static final String CONTROL_RES = "81";
    private static final String QUERY = "82";
    private static final String QUERY_RES = "83";

    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService, IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService) {
        String operType = frameRespData.getOperType();
        switch (operType) {
            case OPREATE_QUERY_RESP:
                iParaPrtclAnalysisService.queryParaResponse(frameRespData);
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
        //外部头信息 6字节
        if (bytes.length <= 6) {
            log.warn("SCMM-2300调制解调器数据帧异常, 响应数据长度错误, 数据体长度:{}, 数据体:{}", bytes.length, HexUtil.encodeHexStr(bytes));
            return frameRespData;
        }
        if (!validAndCheck(socketEntity)) {
            log.warn("SCMM-2300调制解调器数据帧异常，校验和校验错误,  数据体长度:{}, 数据体:{}", bytes.length, HexUtil.encodeHexStr(bytes));
            return frameRespData;
        }
        //按协议中指定的位置获取数据体长度
        int len = bytesToNum(bytes, 1, 1, ByteBuf::readShort);
        int hexLen = Integer.parseInt(HexUtil.toHex(len));
        //响应类型标识
        Short respType = bytesToNum(bytes, 2, 1, ByteBuf::readUnsignedByte);
        String hexRespType = HexUtil.toHex(respType);
        //参数关键字
        Short cmd = bytesToNum(bytes, 4, 1, ByteBuf::readUnsignedByte);
        String hexCmd = HexUtil.toHex(cmd);
        //数据体
        byte[] paramBytes = byteArrayCopy(bytes, 3, hexLen - 2);
        frameRespData.setParamBytes(paramBytes);
        frameRespData.setCmdMark(hexCmd);
        frameRespData.setOperType(hexRespType);
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        byte[] paramBytes = frameReqData.getParamBytes();
        //业务层内容包括 设置单元 + 关键字 + 信息体
        int dataLen = paramBytes.length;
        int frameLenField = dataLen + 2;

        String operType = frameReqData.getOperType();

        String keyWord;
        //获取参数协议
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(frameReqData.getDevType(), frameReqData.getCmdMark());
        FrameParaInfo paraInfo = BaseInfoContainer.getParaInfoByCmd(frameReqData.getDevType(), frameReqData.getCmdMark());
        //todo 设备参数添加所属子单元
        if (prtclFormat == null || prtclFormat.getFmtId() == null) {
            throw new BaseException("设备类型为" + frameReqData.getDevType() + "，参数命令为" + frameReqData.getCmdMark() + "协议格式获取失败...");
        }
        if (OPREATE_QUERY.equals(operType)) {
            keyWord = prtclFormat.getFmtSkey();
        } else {
            keyWord = prtclFormat.getFmtCkey();
        }
        ModemScmmEntity modemScmmEntity = ModemScmmEntity.builder()
                .beginOffset((byte) 0x7E)
                .length((byte) frameLenField)
                .cmd((byte) Integer.parseInt(keyWord, 16))
                .unit((byte)Integer.parseInt(paraInfo.getParaVal(),16))  //todo
                .value(paramBytes)
                .end((byte) 0x7E)
                .build();
        //累加校验和
        byte checkSum = addGetBottom(modemScmmEntity);
        modemScmmEntity.setCheck(checkSum);
        return pack(modemScmmEntity);
    }

    /**
     * 累加从 长度 到 参数体 的所有内容作为校验和
     *
     * @param entity 数据体
     * @return 校验和字节
     */
    private byte addGetBottom(ModemScmmEntity entity) {
        List<byte[]> list = new ArrayList<>();
        list.add(new byte[]{entity.getLength()});
        list.add(new byte[]{entity.getCmd()});
        list.add(new byte[]{entity.getUnit()});
        if (entity.getValue() != null) {
            list.add(entity.getValue());
        }
        byte[] bytes = listToBytes(list);
        return ByteUtils.addGetBottom(bytes, 0, bytes.length);
    }

    private byte[] pack(ModemScmmEntity entity) {
        List<byte[]> list = new ArrayList<>();
        list.add(new byte[]{entity.getBeginOffset()});
        list.add(new byte[]{entity.getLength()});
        list.add(new byte[]{entity.getCmd()});
        list.add(new byte[]{entity.getUnit()});
        if (entity.getValue() != null) {
            list.add(entity.getValue());
        }
        list.add(new byte[]{entity.getCheck()});
        list.add(new byte[]{entity.getEnd()});
        return listToBytes(list);
    }

    /**
     * 转义并校验
     *
     * @param socketEntity 接收到的数据帧结构体
     * @return 校验和是否正确
     */
    private boolean validAndCheck(SocketEntity socketEntity) {
        byte[] bytes = socketEntity.getBytes();
        //todo 接受和发送的转意规则
        return false;
    }

}
