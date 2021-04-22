package com.xy.netdev.sendrecv.head;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.HexUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.modem.ModemPrtcServiceImpl;
import com.xy.netdev.frame.service.modemscmm.ModemScmmPrtcServiceImpl;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.sendrecv.entity.device.ModemScmmEntity;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Autowired
    private ModemScmmPrtcServiceImpl modemScmmPrtcService;

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
        //外部头信息 6字节
        if (bytes.length <= 6) {
            log.warn("SCMM-2300调制解调器数据帧异常, 响应数据长度错误, 数据体长度:{}, 数据体:{}", bytes.length, HexUtil.encodeHexStr(bytes));
            return frameRespData;
        }
        //转义校验并获取转义后的新 bytes
        Pair<Boolean, byte[]> validAndCheck = convertAndCheck(socketEntity);
        if (!validAndCheck.getKey()) {
            log.warn("SCMM-2300调制解调器数据帧异常，校验和校验错误,  数据体长度:{}, 数据体:{}", bytes.length, HexUtil.encodeHexStr(bytes));
//            return frameRespData;
        }
        bytes = validAndCheck.getValue();
        //按协议中指定的位置获取数据体长度
        int len = bytesToNum(bytes, 1, 1, ByteBuf::readUnsignedByte);
        int hexLen = Integer.parseInt(HexUtil.toHex(len),16);
        //TODO 长度校验
        //响应类型标识
        Short respType = bytesToNum(bytes, 2, 1, ByteBuf::readUnsignedByte);
        String hexRespType = HexUtil.toHex(respType);
        //数据体   去掉 命令体+校验字的长度(设置单元+信息体)
        byte[] paramBytes = byteArrayCopy(bytes, 3, hexLen - 2);
        frameRespData.setParamBytes(paramBytes);
        //参数关键字
        if (CONTROL_RES.equals(hexRespType)){
            Short cmd = bytesToNum(bytes, 4, 1, ByteBuf::readUnsignedByte);
            String hexCmd = HexUtil.toHex(cmd);
            //拼接控制响应单元关键字
            String resHexCmd = "000"+hexCmd;
            frameRespData.setCmdMark(resHexCmd);
            frameRespData.setOperType(OPREATE_CONTROL_RESP);
        }else {
            //查询响应设置单元作为关键字
            Short unit = bytesToNum(bytes, 3, 1, ByteBuf::readUnsignedByte);
            String hexUnit = lefPadNumToHexStr(unit);
            frameRespData.setCmdMark(hexUnit);
            frameRespData.setOperType(OPREATE_QUERY_RESP);
        }
        return frameRespData;
    }

    @Override
    public String cmdMarkConvert(FrameRespData frameRespData) {
        String cmdMark = frameRespData.getCmdMark();
        if (cmdMark!=null&&cmdMark.length()==1){
            frameRespData.setCmdMark("0"+cmdMark);
        }
        return frameRespData.getCmdMark();
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        byte[] paramBytes = frameReqData.getParamBytes();
        //业务层内容包括 关键字 + 信息体
        int dataLen = 0;
        if (paramBytes!=null){
            dataLen = paramBytes.length;
        }
        int frameLenField = dataLen + 3;

        String operType = frameReqData.getOperType();
        String accessType = frameReqData.getAccessType();

        String keyWord;
        String unitCode;
        //获取参数协议
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(frameReqData.getDevType(), frameReqData.getCmdMark());
        if (SysConfigConstant.ACCESS_TYPE_INTERF.equals(accessType)){
            Interface linkInterface = BaseInfoContainer.getInterLinkInterface(frameReqData.getDevType(), frameReqData.getCmdMark());
            //备注1 单元编码
            unitCode = linkInterface.getItfCmdMark();
        }else {
            FrameParaInfo paraInfoByCmd = BaseInfoContainer.getParaInfoByCmd(frameReqData.getDevType(), frameReqData.getCmdMark());
            unitCode = paraInfoByCmd.getNdpaRemark1Data();
        }

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
                .unit((byte) Integer.parseInt(unitCode, 16))
                .value(paramBytes)
                .end((byte) 0x7E)
                .build();
        //累加校验和
        byte checkSum = addGetBottom(modemScmmEntity);
        modemScmmEntity.setCheck(checkSum);
        byte[] pack = pack(modemScmmEntity);
        //转义
        byte[] byteReplace = byteReplace(pack, 1, pack.length - 1, Pair.of("7E", "7D5E"), Pair.of("7D", "7D5D"));

        if (OPREATE_QUERY.equals(operType)){
            log.debug("2300调制解调器查询单元：{}, 查询帧：{}",unitCode,HexUtil.encodeHexStr(byteReplace));
        }else {
            log.info("2300调制解调器控制单元：{}, 控制帧：{}",unitCode,HexUtil.encodeHexStr(byteReplace));
        }
        return byteReplace;
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
    private Pair<Boolean, byte[]> convertAndCheck(SocketEntity socketEntity) {
        boolean flag = false;
        byte[] bytes = socketEntity.getBytes();
        byte[] realBytes = byteReplace(bytes, 1, bytes.length - 1, Pair.of("7D5E", "7E"), Pair.of("7D5D", "7D"));
        byte receiveCheckByte = Objects.requireNonNull(byteArrayCopy(realBytes, realBytes.length - 2, 1))[0];
        byte checkByte = ByteUtils.addGetBottom(realBytes, 1, bytes.length - 3);
        if (checkByte==receiveCheckByte){
            flag = true;
        }
        return Pair.of(flag, realBytes);
    }
}
