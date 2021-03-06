package com.xy.netdev.sendrecv.head;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.codec.AscIIParamCodec;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.sendrecv.entity.device.CarAntennaEntity;
import com.xy.netdev.sendrecv.entity.device.ModemEntity;
import com.xy.netdev.sendrecv.entity.device.MstcEntity;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.xy.netdev.common.util.ByteUtils.*;

/**
 * MSCT-102C多体制卫星信道终端站控协议头解析
 *
 * @author luo
 * @date 2021/4/8
 */
@Service
@Slf4j
public class MsctImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {


    /**响应标识数组，用来校验响应帧结构*/
    private static final String[] RESPONSE_SIGNS = {"81","83"};

    @Autowired
    private ISysParamService sysParamService;

    @Override
    public void callback(FrameRespData respData, IParaPrtclAnalysisService iParaPrtclAnalysisService, IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService, ICtrlInterPrtclAnalysisService ctrlInterPrtclAnalysisService) {

        if(respData.getOperType().equals(SysConfigConstant.OPREATE_CONTROL_RESP)) {
            if (ctrlInterPrtclAnalysisService != null) {
                ctrlInterPrtclAnalysisService.ctrlParaResponse(respData);
            } else {
                iParaPrtclAnalysisService.ctrlParaResponse(respData);
            }
        }else{
            if(iQueryInterPrtclAnalysisService!=null){
                iQueryInterPrtclAnalysisService.queryParaResponse(respData);
            }
        }
    }

    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
        byte[] bytes = socketEntity.getBytes();
        //数据体长度
        int len = bytesToNum(bytes, 5, 2, ByteBuf::readShort);
        //响应数据类型标识
        String respStr = new String(HexUtil.encodeHex(socketEntity.getBytes()));
        String hexRespType = respStr.substring(4,6).toUpperCase();
        String hexPrtcCmd = respStr.substring(6,10).toUpperCase();
        String cmdMark = hexPrtcCmd.substring(2);
        if(cmdMark.toUpperCase().equals("AA")){
            if(hexRespType.equals("81")){
                hexPrtcCmd = "80"+ respStr.substring(6,10).toUpperCase();
                log.warn("msct模式切换响应："+ HexUtil.encodeHexStr(bytes).toUpperCase());
            }else{
                hexPrtcCmd = "82"+ respStr.substring(6,10).toUpperCase();
            }
        }else{
            if(hexRespType.equals("81")){
                hexPrtcCmd = "80"+hexPrtcCmd;
            }
        }
        if (!Arrays.asList(RESPONSE_SIGNS).contains(hexRespType)){
            log.error("收到包含错误响应标识的帧结构，标识字节：{}----数据体：{}",hexRespType,bytes);
        }
        //数据体
        byte[] paramBytes = byteArrayCopy(bytes, 7, len);
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(frameRespData.getDevType(), hexPrtcCmd);
        String operateType = BaseInfoContainer.getOptByPrtcl(prtclFormat, hexRespType);
        frameRespData.setOperType(operateType);
        frameRespData.setCmdMark(hexPrtcCmd);
        frameRespData.setParamBytes(paramBytes);
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        byte[] paramBytes = frameReqData.getParamBytes();
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterface(frameReqData.getDevType(), frameReqData.getCmdMark());
        String cmdType;
        if (StrUtil.isNotBlank(prtclFormat.getFmtSkey())){
            cmdType = prtclFormat.getFmtSkey();
        }else {
            cmdType = prtclFormat.getFmtCkey();
        }
        String cmdMark = frameReqData.getCmdMark().toUpperCase();
        if(cmdMark.startsWith("80")){
            cmdMark = cmdMark.substring(2);
        }
        if(cmdMark.contains("AA") && !cmdMark.equals("05AA")){
            cmdMark = cmdMark.substring(2);
        }
        byte[] keywords = HexUtil.decodeHex(cmdMark);
        int length = 0;
        if(paramBytes!=null){
            length = paramBytes.length;
        }
        MstcEntity entity = MstcEntity.builder()
                .beginOffset( new byte[]{(byte)0xEB,(byte)0x90})
                .cmdType((HexUtil.decodeHex(cmdType)[0]))
                .keywords(keywords)
                .length(objToBytes(length, 2))
                .params(paramBytes)
                .build();
        //累加校验和
        byte check = check(entity);
        entity.setCheck(check);
        if(frameReqData.getOperType().equals("0026003")){
            log.warn("MSCT-控制帧："+HexUtil.encodeHexStr(pack(entity)).toUpperCase());
        }
        return pack(entity);
    }

    private byte[] pack(MstcEntity mstcEntity){
        List<byte[]> list = new ArrayList<>();
        list.add(mstcEntity.getBeginOffset());
        list.add(new byte[]{mstcEntity.getCmdType()});
        list.add(mstcEntity.getKeywords());
        list.add(mstcEntity.getLength());
        if (mstcEntity.getParams() != null && mstcEntity.getParams().length > 0){
            list.add(mstcEntity.getParams());
        }
        list.add(new byte[]{mstcEntity.getCheck()});
        byte[] bytes = listToBytes(list);
        return bytes;

    }

    private byte check(MstcEntity entity) {
        List<byte[]> list = new ArrayList<>();
        list.add(entity.getBeginOffset());
        list.add(new byte[]{entity.getCmdType()});
        list.add(entity.getKeywords());
        list.add(entity.getLength());
        if (entity.getParams() != null && entity.getParams().length > 0) {
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

    public static void main(String[] args) {
       byte[] bytes1 = {0x04, 0x10, 0x00, 0x13};
        //byte[] bytes1 = {0xEB, 0x90, 0x82, 0x03, 0xAA, 0x00};
//        AscIIParamCodec paramCodec = new AscIIParamCodec();
//        String decode = paramCodec.decode(bytes);
//        System.out.println(decode);
//        byte[] encode = paramCodec.encode(decode);
        System.out.println(addGetBottom256(bytes1,0,6));
//        byte temp = bytes1[0];
//        for (int i = 1; i < bytes1.length; i++) {
//            temp ^= bytes1[i];
//        }
//        System.out.println(HexUtil.encodeHex(new byte[]{temp}));
    }
}
