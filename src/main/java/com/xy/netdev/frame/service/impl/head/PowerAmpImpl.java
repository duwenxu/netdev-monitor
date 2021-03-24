package com.xy.netdev.frame.service.impl.head;

import cn.hutool.core.util.HexUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.entity.device.PowerAmpEntity;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.PrtclFormat;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.common.constant.SysConfigConstant.*;
import static com.xy.netdev.common.util.ByteUtils.*;

/**
 * Ku4000W功率放大器
 *
 * @author duwenxu
 * @create 2021-03-22 15:23
 */
@Service
@Slf4j
public class PowerAmpImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {

    @Autowired
    private ISysParamService sysParamService;
    /**查询/控制响应命令标识*/
    private static final String QUERY_RES = "83";
    private static final String CONTROL_RES = "81";
    private static final String QUERY_CMD ="82";

    /**
     * 数据接收解析回调
     *
     * @param frameRespData                   数据帧结构与
     * @param iParaPrtclAnalysisService       参数响应处理类
     * @param iQueryInterPrtclAnalysisService 接口响应处理类
     */
    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService, IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService) {
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
        if (bytes.length <= 8) {
            log.warn("Ku4000W功率放大器响应数据长度错误, 未能正确解析, 数据体长度:{}, 数据体:{}", bytes.length, HexUtil.encodeHexStr(bytes));
            return frameRespData;
        }
        //按协议中指定的位置获取数据体长度
        int len = bytesToNum(bytes, 5, 2, ByteBuf::readShort);
        int hexLen = Integer.parseInt(HexUtil.toHex(len));
        //响应类型标识
        Short respType = bytesToNum(bytes, 7, 1, ByteBuf::readUnsignedByte);
        String hexRespType = HexUtil.toHex(respType);

        //数据体
        byte[] paramBytes = byteArrayCopy(bytes, 8, hexLen);
        //判断操作类型赋值
        if (QUERY_RES.equals(hexRespType)){
            frameRespData.setCmdMark(QUERY_CMD);
            frameRespData.setOperType(OPREATE_QUERY_RESP);
        }else if (CONTROL_RES.equals(hexRespType)){
            frameRespData.setOperType(OPREATE_CONTROL_RESP);
            //参数关键字
            Byte cmd = bytesToNum(bytes, 8, 1, ByteBuf::readByte);
            String hexCmd = numToHexStr(Long.valueOf(cmd));
            frameRespData.setCmdMark(hexCmd);
        }
        frameRespData.setParamBytes(paramBytes);
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        //参数数据
        byte[] paramBytes = frameReqData.getParamBytes();
        //数据长度
        int dataLength = 1;
        if (paramBytes != null) {
            //命令字+参数体 之外的固定长度 9
            dataLength = paramBytes.length;
        }
        String subTypeCode = getDevSubCode(frameReqData);
        String operType = frameReqData.getOperType();
        String keyWord;
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(frameReqData.getDevType(), frameReqData.getCmdMark());
        if (prtclFormat==null|| prtclFormat.getFmtId()==null){
            throw new BaseException("设备类型为"+frameReqData.getDevType()+"，参数命令为"+frameReqData.getCmdMark()+"协议格式获取失败...");
        }
        if (OPREATE_QUERY.equals(operType)){
            keyWord = prtclFormat.getFmtSkey();
        }else {
            keyWord = prtclFormat.getFmtCkey();
        }
        if (StringUtils.isBlank(subTypeCode)){
            throw new BaseException("编号为"+frameReqData.getDevNo()+"子设备编码为null...");
        }
        PowerAmpEntity ampEntity = PowerAmpEntity.builder()
                .beginOffset((byte) 0x7E)
                .devType((byte) 0x21)
                .devSubType(Byte.valueOf(subTypeCode,16))
                .deviceAddress(new byte[]{0x00, 0x01})
                .length(objToBytes(dataLength, 2))
                .cmd((byte)Integer.parseInt(keyWord,16))
                .params(paramBytes)
                .end((byte) 0x7E)
                .build();
        //累加校验和
        byte checkSum = addGetBottom(ampEntity);
        ampEntity.setCheck(checkSum);
        return pack(ampEntity);
    }

    /**
     * 累加从 地址 到 参数体 的所有内容作为校验和
     * @param ampEntity 数据体
     * @return 校验和字节
     */
    private byte addGetBottom(PowerAmpEntity ampEntity) {
        List<byte[]> list = new ArrayList<>();
        list.add(ampEntity.getDeviceAddress());
        list.add(ampEntity.getLength());
        list.add(new byte[]{ampEntity.getCmd()});
        if (ampEntity.getParams() != null){
            list.add(ampEntity.getParams());
        }
        byte[] bytes = listToBytes(list);
        return ByteUtils.addGetBottom(bytes, 0, bytes.length);
    }

    private byte[] pack(PowerAmpEntity ampEntity) {
        List<byte[]> list = new ArrayList<>();
        list.add(new byte[]{ampEntity.getBeginOffset()});
        list.add(new byte[]{ampEntity.getDevType()});
        list.add(new byte[]{ampEntity.getDevSubType()});
        list.add(ampEntity.getDeviceAddress());
        list.add(ampEntity.getLength());
        list.add(new byte[]{ampEntity.getCmd()});
        if (ampEntity.getParams() != null){
            list.add(ampEntity.getParams());
        }
        list.add(new byte[]{ampEntity.getCheck()});
        list.add(new byte[]{ampEntity.getEnd()});
        return listToBytes(list);
    }

    /**
     * 获取子设备型号 编码
     * @param frameReqData 数据结构
     * @return 子设备编码
     */
    private String getDevSubCode(FrameReqData frameReqData) {
        BaseInfo baseInfo = BaseInfoContainer.getDevInfoByNo(frameReqData.getDevNo());
        String devSubType = baseInfo.getDevSubType();
        if (devSubType!=null){
            return sysParamService.getParaRemark1(devSubType);
        }
        return null;
    }
}
