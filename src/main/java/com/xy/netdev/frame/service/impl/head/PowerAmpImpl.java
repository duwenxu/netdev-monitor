//package com.xy.netdev.frame.service.impl.head;
//
//import cn.hutool.core.util.HexUtil;
//import com.xy.netdev.container.BaseInfoContainer;
//import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
//import com.xy.netdev.frame.bo.FrameReqData;
//import com.xy.netdev.frame.bo.FrameRespData;
//import com.xy.netdev.frame.entity.SocketEntity;
//import com.xy.netdev.frame.entity.device.PowerAmpEntity;
//import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
//import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
//import com.xy.netdev.monitor.entity.BaseInfo;
//import com.xy.netdev.monitor.entity.PrtclFormat;
//import io.netty.buffer.ByteBuf;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import static com.xy.netdev.common.constant.SysConfigConstant.OPREATE_CONTROL_RESP;
//import static com.xy.netdev.common.constant.SysConfigConstant.OPREATE_QUERY_RESP;
//import static com.xy.netdev.common.util.ByteUtils.*;
//
///**
// * Ku4000W功率放大器
// *
// * @author duwenxu
// * @create 2021-03-22 15:23
// */
//@Service
//@Slf4j
//public class PowerAmpImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {
//
//    /**
//     * 数据接收解析回调
//     *
//     * @param frameRespData                   数据帧结构与
//     * @param iParaPrtclAnalysisService       参数响应处理类
//     * @param iQueryInterPrtclAnalysisService 接口响应处理类
//     */
//    @Override
//    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService, IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService) {
//        String operType = frameRespData.getOperType();
//        switch (operType) {
//            case OPREATE_QUERY_RESP:
//                iQueryInterPrtclAnalysisService.queryParaResponse(frameRespData);
//                break;
//            case OPREATE_CONTROL_RESP:
//                iParaPrtclAnalysisService.ctrlParaResponse(frameRespData);
//                break;
//            default:
//                log.warn("设备:{},未知的参数类型:{}", frameRespData.getDevNo(), frameRespData.getCmdMark());
//                break;
//        }
//    }
//
//    @Override
//    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
//        byte[] bytes = socketEntity.getBytes();
//        if (bytes.length <= 8) {
//            log.warn("Ku4000W功率放大器响应数据长度错误, 未能正确解析, 数据体长度:{}, 数据体:{}", bytes.length, HexUtil.encodeHexStr(bytes));
//            return frameRespData;
//        }
//        //按协议中指定的位置获取数据体长度
//        int len = bytesToNum(bytes, 4, 2, ByteBuf::readShort);
//        //响应类型标识
//        Byte respType = bytesToNum(bytes, 7, 1, ByteBuf::readByte);
//        String hexRespType = numToHexStr(Long.valueOf(respType));
//        //参数关键字
//        Byte cmd = bytesToNum(bytes, 8, 1, ByteBuf::readByte);
//        String hexCmd = numToHexStr(Long.valueOf(cmd));
//        //数据体
//        byte[] paramBytes = byteArrayCopy(bytes, 6, len);
//        //业务数据结构包装
//        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(frameRespData.getDevType(), hexCmd);
//        String operateType = BaseInfoContainer.getOptByPrtcl(prtclFormat, hexRespType);
//        frameRespData.setOperType(operateType);
//        frameRespData.setCmdMark(hexCmd);
//        frameRespData.setParamBytes(paramBytes);
//        return frameRespData;
//    }
//
//    @Override
//    public byte[] pack(FrameReqData frameReqData) {
//        //参数数据
//        byte[] paramBytes = frameReqData.getParamBytes();
//        //数据长度
//        int dataLength = 0;
//        if (paramBytes != null) {
//            //命令字+参数体 之外的固定长度 9
//            dataLength = paramBytes.length + 9;
//        }
//        String subTypeCode = getDevSubCode(frameReqData);
//        PowerAmpEntity.builder()
//                .beginOffset((byte) 0x7E)
//                .devType((byte) 0x21)
//                .devSubType(Byte.valueOf(subTypeCode, 16))
//                .deviceAddress(new byte[]{0x00,0x01})
//                .length(byt)
//                .cmd(paramBytes)
//                .params((byte) 0)
//                .check((byte) 0x7D)
//                .end()
//                .build();
//        return new byte[0];
//    }
//
//    private String getDevSubCode(FrameReqData frameReqData) {
//        BaseInfo baseInfo = BaseInfoContainer.getDevInfoByNo(frameReqData.getDevNo());
//        String devSubType = baseInfo.getDevSubType();
//        if ()
//    }
//}
