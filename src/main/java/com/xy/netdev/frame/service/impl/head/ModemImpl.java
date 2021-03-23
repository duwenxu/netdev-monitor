package com.xy.netdev.frame.service.impl.head;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.entity.device.ModemEntity;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.modem.ModemPrtcServiceImpl;
import com.xy.netdev.monitor.entity.PrtclFormat;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.*;

/**
 * 调制解调器
 */
@Service
@Slf4j
public class ModemImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData>{

    /**响应标识数组，用来校验响应帧结构*/
    private static final String[] RESPONSE_SIGNS = {"13","01"};

    @Autowired
    private ModemPrtcServiceImpl prtcService;

    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService,
                         IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService) {
        switch (frameRespData.getOperType()) {
            case SysConfigConstant.OPREATE_QUERY_RESP:
                prtcService.queryParaResponse(frameRespData);
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
        if (bytes.length<=6){
            log.warn("调制解调器数据长度错误, 未能正确解析, 数据体长度:{}, 数据体:{}", bytes.length, HexUtil.encodeHexStr(bytes));
            return frameRespData;
        }
        //数据体长度
        int len = bytesToNum(bytes, 1, 2, ByteBuf::readShort) - 4;
        //响应数据类型标识   查询0X53 控制0X41
        Byte respType = bytesToNum(bytes, 5, 1, ByteBuf::readByte);
        String hexRespType = lefPadNumToHexStr(Long.valueOf(respType));
        if (!Arrays.asList(RESPONSE_SIGNS).contains(hexRespType)){
            log.error("收到包含错误响应标识的帧结构，标识字节：{}----数据体：{}",hexRespType,bytes);
        }

        //参数命令标识
        Byte cmd = bytesToNum(bytes, 6, 1, ByteBuf::readByte);
        //数据体
        byte[] paramBytes = byteArrayCopy(bytes, 6, len);
        String hexCmd = lefPadNumToHexStr(Long.valueOf(cmd));

        //获取操作类型
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(frameRespData.getDevType(), hexCmd);
        String operateType = BaseInfoContainer.getOptByPrtcl(prtclFormat, hexRespType);
        frameRespData.setOperType(operateType);
        frameRespData.setCmdMark(hexCmd);
        frameRespData.setParamBytes(paramBytes);
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        byte[] paramBytes = frameReqData.getParamBytes();
        int len = paramBytes.length + 8;

        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByPara(frameReqData.getDevType(), frameReqData.getCmdMark());
        String keyword;
        if (StrUtil.isNotBlank(prtclFormat.getFmtSkey())){
            keyword = prtclFormat.getFmtSkey();
        }else {
            keyword = prtclFormat.getFmtCkey();
        }
        ModemEntity modemEntity = ModemEntity.builder()
                .beginOffset((byte)0x02)
                .num(ByteUtils.objToBytes(len, 2))
                .deviceType((byte)0x65)
                .deviceAddress((byte)0x01)
                .cmd(Byte.valueOf(keyword, 16))
                .params(paramBytes)
                .check((byte)0)
                .end((byte)0x0A)
                .build();
        byte check = check(modemEntity);
        modemEntity.setCheck(check);
        return pack(modemEntity);
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

    /**
     * 生成数据检测位
     * @param modemEntity 调制解调器模型
     * @return 校验位
     */
    private static byte check(ModemEntity modemEntity){
        return addDiv(
                byteToInt(modemEntity.getNum())
                , byteToInt(modemEntity.getDeviceType())
                , byteToInt(modemEntity.getDeviceType())
                , byteToInt(modemEntity.getDeviceAddress())
                , byteToInt(modemEntity.getCmd())
                , byteToInt(modemEntity.getParams())
                );
    }

    /**
     * 累加取模
     * @param values 数据值
     * @return 校验位
     */
    private static byte addDiv(int... values){
        double div = NumberUtil.div(Arrays.stream(values).sum(), 256);
        return (byte)Double.valueOf(div).intValue();
    }

    /**
     * 数组转十六进制字符串并补全
     * @param num
     * @return
     */
    private static String lefPadNumToHexStr(long num){
        return StringUtils.leftPad(HexUtil.toHex(num), 2,'0').toUpperCase();
    }

}
