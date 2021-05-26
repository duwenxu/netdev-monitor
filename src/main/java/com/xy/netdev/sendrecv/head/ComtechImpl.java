package com.xy.netdev.sendrecv.head;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.Charsets;
import com.xy.common.exception.BaseException;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.rpt.enums.AsciiEnum;
import com.xy.netdev.rpt.enums.ComtechSpeComEnum;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.sendrecv.entity.device.ComtechEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.xy.netdev.common.constant.SysConfigConstant.*;
import static com.xy.netdev.common.util.ByteUtils.*;
import static com.xy.netdev.monitor.constant.MonitorConstants.READ_ONLY;

/**
 * Comtech功率放大器
 * 目前采用 不可打印的协议 可打印的协议无法区分响应类型
 *
 * @author duwenxu
 * @create 2021-05-20 9:33
 */
@Service
@Slf4j
public class ComtechImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {

    //TODO 地址字节是否固定
    private static final String ADDRESS="A";
    //响应头数组     ACK:0X06   NAK:0X15
    private static final List<Byte> RESP_ARR = Arrays.asList((byte)0x06,(byte)0x15);

    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService, IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService, ICtrlInterPrtclAnalysisService ctrlInterPrtclAnalysisService) {
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
        //头字节
        byte startByte = bytes[0];
        log.info("Comtech功率放大器收到响应帧：[{}]", HexUtil.encodeHexStr(bytes));
        if (!RESP_ARR.contains(startByte)) {
            log.warn("Comtech功率放大器响应帧头错误, 未能正确解析, 数据体:{}", HexUtil.encodeHexStr(bytes));
            return frameRespData;
        }
        //使用响应码标记 响应接收/响应拒绝
        if (startByte==RESP_ARR.get(0)){ //ACK
            frameRespData.setRespCode("0");
        }else {
            frameRespData.setRespCode("1");
        }

        //数据体长度 总长度-首尾字节长度
        //首字节 STX+地址  尾字节 EXT+CHECK+0d0a 共6字节
        int contextLen = bytes.length - 6;
        byte[] context = byteArrayCopy(bytes, 2, contextLen);
        if (context==null||context.length==0){
            log.warn("Comtech功率放大器响应内容为空, 未能正确解析, 数据体:{}", HexUtil.encodeHexStr(bytes));
        }
        Assert.assertNotNull(context);
        String cmk = "";
        //校验是否为特殊查询符号
        if (context.length>3){
            byte[] cmdBytes = byteArrayCopy(context, 0, 2);
            if (Arrays.equals(ComtechSpeComEnum.PBM.getBytes(), cmdBytes)){
                cmk = ComtechSpeComEnum.PBM.getRespCommand();
            }else if (Arrays.equals(ComtechSpeComEnum.PBW.getBytes(), cmdBytes)){
                cmk = ComtechSpeComEnum.PBW.getRespCommand();
            }else {
                //处理单字节的cmd
                cmk = StrUtil.str(new byte[]{context[0]},StandardCharsets.UTF_8);
            }
        }else {
            //处理单字节的cmd
            cmk = StrUtil.str(new byte[]{context[0]},StandardCharsets.UTF_8);
        }
        frameRespData.setCmdMark(cmk);
        frameRespData.setParamBytes(context);

        FrameParaInfo para = BaseInfoContainer.getParaInfoByCmd(COMTECH_GF, cmk);
        if (para.getParaId() == null){
            log.warn("Comtech功放cmd:[{}]未查询到对应的参数",cmk);
            throw new BaseException("Comtech功放cmd:"+cmk+" 未查询到对应的参数");
        }
        String accessRight = para.getNdpaAccessRight();
        //只读参数为查询，否则为控制
        if (READ_ONLY.equals(accessRight)){
            frameRespData.setOperType(OPREATE_QUERY_RESP);
        }else {
            frameRespData.setOperType(OPREATE_CONTROL_RESP);
        }
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
        ComtechEntity comtechEntity;
        comtechEntity = ComtechEntity.builder()
                .start(AsciiEnum.STX.getCode())
                .address(StrUtil.bytes(ADDRESS)[0])
                .command(StrUtil.bytes(cmdMark))
                .parameters(paramBytes)
                .end(AsciiEnum.EXT.getCode())
                .build();
        byte check = xorCheck(comtechEntity);
        comtechEntity.setCheck(check);
        byte[] pack = pack(comtechEntity);

        //字符串转换方式处理
        String checkStr = StrUtil.str(check, Charsets.UTF_8);
        sb.append(checkStr);
        byte[] bytes = sb.toString().getBytes();

        log.info("Comtech发送查询帧：查询命令字：[{}]，查询帧：[{}]",cmdMark,HexUtil.encodeHexStr(pack));
        return pack;
    }

    /**
     * 异或获取校验和
     *
     * @param entity
     * @return
     */
    private static byte xorCheck(ComtechEntity entity) {
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


    private static byte[] pack(ComtechEntity entity) {
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

    public static void main(String[] args) {
        ComtechEntity comtechEntity = ComtechEntity.builder()
                .start(AsciiEnum.STX.getCode())
                .address(StrUtil.bytes(ADDRESS)[0])
                .command(StrUtil.bytes("@"))
                .end(AsciiEnum.EXT.getCode())
                .build();
        byte check =xorCheck(comtechEntity);
        comtechEntity.setCheck(check);
        byte[] pack = pack(comtechEntity);
        log.info("生成的控制字节为：{}",HexUtil.encodeHexStr(pack));
    }
}
