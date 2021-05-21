package com.xy.netdev.sendrecv.head;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.Charsets;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.rpt.enums.AsciiEnum;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.sendrecv.entity.device.ComtechEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.xy.netdev.common.constant.SysConfigConstant.OPREATE_CONTROL_RESP;
import static com.xy.netdev.common.constant.SysConfigConstant.OPREATE_QUERY_RESP;
import static com.xy.netdev.common.util.ByteUtils.*;

/**
 * Comtech功率放大器
 * 目前采用 不可打印的协议
 *
 * @author duwenxu
 * @create 2021-05-20 9:33
 */
@Service
@Slf4j
public class ComtechImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {

    //TODO 地址字节是否固定
    private static final String ADDRESS="A";

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
        String data = new String(bytes, Charset.defaultCharset());
        log.info("Comtech功率放大器收到响应帧：[{}]", HexUtil.encodeHexStr(bytes));
        //todo 解包时是否是以 ACK/NAK开头的String   同时赋值响应类型
//        if (!data.startsWith(AsciiEnum.ACK.getName()) && !data.startsWith(AsciiEnum.NAK.getName())) {
//            log.warn("Comtech功率放大器响应帧头错误, 未能正确解析, 数据体长·度:{}, 数据体:{}", bytes.length, HexUtil.encodeHexStr(bytes));
//            return frameRespData;
//        }
        //数据体长度 总长度-首尾字节长度
        int contextLen = bytes.length - 4;
        byte[] context = byteArrayCopy(bytes, 2, contextLen);
        //TODO 单字节的cmd
        String cmk = HexUtil.encodeHexStr(Objects.requireNonNull(byteArrayCopy(bytes, 2, 1))).toUpperCase();

        frameRespData.setParamBytes(context);
        frameRespData.setCmdMark(cmk);
        //命令字+数据体
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        StringBuilder sb = new StringBuilder();
        sb.append(AsciiEnum.STX.getCode());
        sb.append(ADDRESS);
        String cmdMark = frameReqData.getCmdMark();
        sb.append(cmdMark);
        sb.append(AsciiEnum.EXT.getCode());

        byte[] paramBytes = frameReqData.getParamBytes();
        ComtechEntity comtechEntity = null;
        try {
            comtechEntity = ComtechEntity.builder()
                    .start(AsciiEnum.STX.getCode())
                    .address(StrUtil.bytes(ADDRESS)[0])
                    .command(ByteUtils.objectToByte(cmdMark))
                    .parameters(paramBytes)
                    .end(AsciiEnum.EXT.getCode())
                    .build();
        } catch (IOException e) {
           log.error("命令字节转换错误：cmdMark={},error:{}",cmdMark,e.getMessage());
        }
        byte check = xorCheck(comtechEntity);
        comtechEntity.setCheck(check);
        byte[] pack = pack(comtechEntity);

        //字符串转换方式处理
        String checkStr = StrUtil.str(check, Charsets.UTF_8);
        sb.append(checkStr);
        byte[] bytes = sb.toString().getBytes();
        log.info("Comtech发送查询帧：[{}]",HexUtil.encodeHexStr(pack));
        return pack;
    }

    /**
     * 异或获取校验和
     *
     * @param entity
     * @return
     */
    private byte xorCheck(ComtechEntity entity) {
        byte[] bytes = new byte[]{entity.getStart(), entity.getAddress()};
        bytes = bytesMerge(bytes, entity.getCommand());
        if (entity.getParameters()!= null){
            bytes = bytesMerge(bytes, entity.getParameters());
        }
        bytes = bytesMerge(bytes,new byte[]{entity.getEnd()});
        byte temp = bytes[0];
        for (int i = 1; i < bytes.length; i++) {
            temp ^= bytes[i];
        }
        return temp;
    }

    private byte[] pack(ComtechEntity entity) {
        List<byte[]> list = new ArrayList<>();
        list.add(new byte[]{entity.getStart()});
        list.add(new byte[]{entity.getAddress()});
        list.add(entity.getCommand());
        if (entity.getParameters() != null){
            list.add(entity.getParameters());
        }
        list.add(new byte[]{entity.getEnd()});
        list.add(new byte[]{entity.getCheck()});
        return listToBytes(list);
    }
}
