package com.xy.netdev.sendrecv.head;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.modem.ModemPrtcServiceImpl;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.sendrecv.entity.device.ModemEntity;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.xy.netdev.common.constant.SysConfigConstant.*;
import static com.xy.netdev.common.util.ByteUtils.*;

/**
 * 650-调制解调器
 */
@Service
@Slf4j
public class ModemImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData>{

    /**响应标识数组，用来校验响应帧结构*/
    private static final String[] RESPONSE_SIGNS = {"53","41"};

    @Autowired
    private ModemPrtcServiceImpl prtcService;

    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService,
                         IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService, ICtrlInterPrtclAnalysisService ctrlInterPrtclAnalysisService) {
        String operType = frameRespData.getOperType();
        String accessType = frameRespData.getAccessType();
        switch (operType) {
            case SysConfigConstant.OPREATE_QUERY_RESP:
                if (ACCESS_TYPE_INTERF.equals(accessType)){
                    iQueryInterPrtclAnalysisService.queryParaResponse(frameRespData);
                }else {
                    //目前不使用单参数查询
                    iParaPrtclAnalysisService.queryParaResponse(frameRespData);
                }
                break;
            case SysConfigConstant.OPREATE_CONTROL_RESP:
                prtcService.ctrlParaResponse(frameRespData);
                break;
            default:
                log.warn("设备:{},未知调制解调器参数类型:{}", frameRespData.getDevNo(), frameRespData.getCmdMark());
                break;
        }
    }


    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
        byte[] bytes = socketEntity.getBytes();
        log.info("650调制解调器接收到响应帧：{}",HexUtil.encodeHexStr(bytes));
        if (bytes.length<=6){
            log.warn("650调制解调器数据长度错误, 未能正确解析, 数据体长度:{}, 数据体:{}", bytes.length, HexUtil.encodeHexStr(bytes));
            return frameRespData;
        }
        //数据体长度
        int len = bytesToNum(bytes, 1, 2, ByteBuf::readShort) - 4;
        //响应数据类型标识   查询0X53 控制0X41
        Byte respType = bytesToNum(bytes, 5, 1, ByteBuf::readByte);
        String hexRespType = numToHexStr(Long.valueOf(respType));
        if (!Arrays.asList(RESPONSE_SIGNS).contains(hexRespType)){
            log.error("收到包含错误响应标识的帧结构，标识字节：{}----数据体：{}",hexRespType,HexUtil.encodeHexStr(bytes));
        }
        //TODO 这里的查询相应暂时按接口查询来配置，待后续具体讨论后再修改
//        //参数命令标识
//        Byte cmd = bytesToNum(bytes, 6, 1, ByteBuf::readByte);
//        String hexCmd = numToHexStr(Long.valueOf(cmd));
        //数据体
        byte[] paramBytes = byteArrayCopy(bytes, 6, len);

        //获取操作类型
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(frameRespData.getDevType(), hexRespType);
        String operateType = BaseInfoContainer.getOptByPrtcl(prtclFormat, hexRespType);
        frameRespData.setOperType(operateType);
        frameRespData.setCmdMark(hexRespType);
        frameRespData.setParamBytes(paramBytes);
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        byte[] paramBytes = frameReqData.getParamBytes();
        int len = 4;
        if (paramBytes != null && paramBytes.length != 0) {
            len = paramBytes.length + 4;
        }
        PrtclFormat prtclFormat;
        String operateType = frameReqData.getAccessType();
        String operType = frameReqData.getOperType();
        if (ACCESS_TYPE_INTERF.equals(operateType)) {
            prtclFormat = BaseInfoContainer.getPrtclByInterface(frameReqData.getDevType(), frameReqData.getCmdMark());
        } else {
            prtclFormat = BaseInfoContainer.getPrtclByPara(frameReqData.getDevType(), frameReqData.getCmdMark());
        }
        String keyword;
        if (OPREATE_QUERY.equals(operType)) {
            keyword = prtclFormat.getFmtSkey();
        } else {
            keyword = prtclFormat.getFmtCkey();
        }
        ModemEntity modemEntity = ModemEntity.builder()
                .beginOffset((byte) 0x02)
                .num(ByteUtils.objToBytes(len, 2))
                .deviceType((byte) 0x65)
                .deviceAddress((byte) 0x01)
                .cmd(Byte.valueOf(keyword, 16))
                .params(paramBytes)
                .check((byte) 0)
                .end((byte) 0x0A)
                .build();
        byte check = check(modemEntity);
        modemEntity.setCheck(check);
        return pack(modemEntity);
    }

    /**
     * 校验和  规则：字节累加和模256
     * @param entity  调制解调器实体类
     * @return 校验和
     */
    private byte check(ModemEntity entity) {
        List<byte[]> list = new ArrayList<>();
        list.add(entity.getNum());
        list.add(new byte[]{entity.getDeviceType()});
        list.add(new byte[]{entity.getDeviceAddress()});
        list.add(new byte[]{entity.getCmd()});
        if (entity.getParams() != null) {
            list.add(entity.getParams());
        }
        byte[] bytes = listToBytes(list);
        return addGetBottom256(bytes, 0, bytes.length);
    }

    /**
     * 校验和  规则：字节累加和模256
     * @param bytes 原始数组
     * @param offset 起始位
     * @param len 长度
     * @return 低位
     */
    public static byte addGetBottom256(byte[] bytes, int offset, int len) {
        byte[] arrayCopy = byteArrayCopy(bytes, offset, len);
        int sum = 0;
        for (byte b : Objects.requireNonNull(arrayCopy)) {
            sum += (b & 0xFF);
        }
        return (byte) Double.valueOf(sum % 256).intValue();
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

}
