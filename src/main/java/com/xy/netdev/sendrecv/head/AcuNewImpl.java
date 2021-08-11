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
import com.xy.netdev.sendrecv.entity.device.AcuNewEntity;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.xy.netdev.common.constant.SysConfigConstant.*;
import static com.xy.netdev.common.util.ByteUtils.*;

/**
 * 7.3mAcu天线单元协议头解析
 *
 * @author duwenxu
 * @create 2021-08-09 11:34
 */
@Component
@Slf4j
public class AcuNewImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {
    /**
     * 帧头固定长度
     */
    private static final int HEAD_LEN = 11;
    /**
     * 信源/信宿地址
     */
    private static final byte ACU_ADDR = 0x07;
    private static final byte SMCU_ADDR = 0x01;
    private static final byte[] RETAIN_BYTES = new byte[]{0x00, 0x00};
    /**
     * 包序号 发送方需对其进行递增
     */
    private static final AtomicInteger PACKAGE_ID = new AtomicInteger(0);

    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService, IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService, ICtrlInterPrtclAnalysisService ctrlInterPrtclAnalysisService) {
        String opeType = frameRespData.getOperType();
        switch (opeType) {
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
        String devType = frameRespData.getDevType();
        log.debug("7.3mACU天线设备收到响应帧：[{}]", HexUtil.encodeHexStr(bytes));
        if (bytes.length < HEAD_LEN) {
            log.warn("7.3mACU天线设备数据帧异常, 响应数据长度错误, 数据体长度:{}, 数据体:{}", bytes.length, HexUtil.encodeHexStr(bytes));
            return frameRespData;
        }
        //包序号
        int packageId = byteToNumber(bytes, 0, 2).intValue();
        log.info("接收到当前数据包序号：[{}]", packageId);
        PACKAGE_ID.set(packageId);
        //信源地址
        byte[] source = byteArrayCopy(bytes, 2, 1);
        Assert.isTrue(ACU_ADDR == Objects.requireNonNull(source)[0], "7.3mACU天线设备数据帧异常：信源地址错误");
        //信宿地址
        byte[] dest = byteArrayCopy(bytes, 3, 1);
        Assert.isTrue(SMCU_ADDR == Objects.requireNonNull(dest)[0], "7.3mACU天线设备数据帧异常：信宿地址错误");
        //数据类型
        Short dataType = bytesToNum(bytes, 6, 1, ByteBuf::readUnsignedByte);
        String hexType = "0" + HexUtil.toHex(dataType);
        //数据长度
        Long len = bytesToNum(bytes, 7, 4, ByteBuf::readUnsignedInt);
        //数据正文
        byte[] contentBytes = byteArrayCopy(bytes, 11, bytes.length - 11);
        Assert.isTrue(len == Objects.requireNonNull(contentBytes).length, "7.3mACU天线设备数据帧异常：数据长度字段与内容长度不匹配");
        frameRespData.setCmdMark(hexType);
        frameRespData.setParamBytes(contentBytes);

        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterface(devType, hexType);
        String opeType = null;
        if (prtclFormat != null) {
            opeType = BaseInfoContainer.getOptByPrtcl(prtclFormat, hexType);
        }
        if (OPREATE_CONTROL_RESP.equals(opeType)) {
            frameRespData.setOperType(OPREATE_CONTROL_RESP);
            log.info("7.3mACU天线设备收到控制响应数据：[{}]", HexUtil.encodeHexStr(contentBytes));
        } else {
            frameRespData.setOperType(OPREATE_QUERY_RESP);
            log.info("7.3mACU天线设备收到查询响应数据：[{}]", HexUtil.encodeHexStr(contentBytes));
        }
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        byte[] paramBytes = frameReqData.getParamBytes();
        String cmdMark = frameReqData.getCmdMark();
        //包序号
        PACKAGE_ID.set(getPackageId());
        //正文长度
        int dataLen = 0;
        if (paramBytes != null) {
            dataLen = paramBytes.length;
        }

        String operType = frameReqData.getOperType();
        String cmkWord;
        //获取参数协议
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(frameReqData.getDevType(), cmdMark);
        if (prtclFormat == null || prtclFormat.getFmtId() == null) {
            throw new BaseException("设备类型为" + frameReqData.getDevType() + "，参数命令为" + cmdMark + "协议格式获取失败...");
        }
        if (OPREATE_QUERY.equals(operType)) {
            cmkWord = prtclFormat.getFmtSkey();
        } else {
            cmkWord = prtclFormat.getFmtCkey();
        }
        AcuNewEntity acuNewEntity = AcuNewEntity.builder()
                .packageSign(ByteUtils.objToBytes(PACKAGE_ID.get(), 2))
                .len(ByteUtils.objToBytes(dataLen, 4))
                .source(SMCU_ADDR)
                .dest(ACU_ADDR)
                .retain(RETAIN_BYTES)
                .dataType((byte) Integer.parseInt(cmkWord, 16))
                .data(paramBytes)
                .build();
        byte[] pack = pack(acuNewEntity);
        if (OPREATE_QUERY.equals(operType)) {
            log.info("7.3mACU天线设备发送查询帧：{}", HexUtil.encodeHexStr(pack));
        } else {
            log.info("7.3mACU天线设备发送控制帧：{}", HexUtil.encodeHexStr(pack));
        }
        return pack;
    }

    private byte[] pack(AcuNewEntity entity) {
        List<byte[]> list = new ArrayList<>();
        list.add(entity.getPackageSign());
        list.add(new byte[]{entity.getSource()});
        list.add(new byte[]{entity.getDest()});
        list.add(entity.getRetain());
        list.add(new byte[]{entity.getDataType()});
        list.add(entity.getLen());
        if (entity.getData() != null) {
            list.add(entity.getData());
        }
        return listToBytes(list);
    }

    /**
     * 获取包序号
     *
     * @return 最新的包序号
     */
    private int getPackageId() {
        return PACKAGE_ID.get() == (2 ^ 16 - 1) ? 0 : PACKAGE_ID.incrementAndGet();
    }
}
