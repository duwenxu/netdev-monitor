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
import com.xy.netdev.monitor.entity.BaseInfo;
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

    @Autowired
    private ModemPrtcServiceImpl prtcService;

    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService,
                         IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService) {
//        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(frameRespData.getDevType(), frameRespData.getCmdMark());
//        if (prtclFormat!=null && prtclFormat.getFmtId()!=null){
//            String keyWord;
//            if (SysConfigConstant.OPREATE_QUERY_RESP.equals(frameRespData.getOperType())){
//                keyWord = prtclFormat.getFmtSckey();
//            }else {
//                keyWord = prtclFormat.getFmtCckey();
//            }
        switch (frameRespData.getOperCode()) {
            case "53":
                prtcService.queryParaResponse(frameRespData);
                break;
            case "41":
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
        //数据体长度
        int len = bytesToNum(bytes, 1, 2, ByteBuf::readShort) - 4;
        //响应数据类型
        Byte respType = bytesToNum(bytes, 5, 1, ByteBuf::readByte);
        //参数命令标识
        Byte cmd = bytesToNum(bytes, 6, 1, ByteBuf::readByte);
        //数据体
        byte[] paramBytes = byteArrayCopy(bytes, 6, len);
        String hexRespType = numToHexStr(Long.valueOf(respType));
        String hexCmd = numToHexStr(Long.valueOf(cmd));
        frameRespData.setCmdMark(hexCmd);
        frameRespData.setOperCode(hexRespType);
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


    public byte check(ModemEntity modemEntity){
        return addDiv(
                byteToInt(modemEntity.getNum())
                , byteToInt(modemEntity.getDeviceType())
                , byteToInt(modemEntity.getDeviceType())
                , byteToInt(modemEntity.getDeviceAddress())
                , byteToInt(modemEntity.getCmd())
                , byteToInt(modemEntity.getParams())
                );
    }

    private byte addDiv(int... values){
        double div = NumberUtil.div(Arrays.stream(values).sum(), 256);
        return (byte)Double.valueOf(div).intValue();
    }

    private static String numToHexStr(long num){
        return StringUtils.leftPad(HexUtil.toHex(num), 2,'0').toUpperCase();
    }
}
