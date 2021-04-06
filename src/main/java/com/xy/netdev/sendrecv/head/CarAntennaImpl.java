package com.xy.netdev.sendrecv.head;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.NumberUtil;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.dzt.DztCtrlInterPrtcServiceImpl;
import com.xy.netdev.frame.service.dzt.DztQueryInterPrtcServiceImpl;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.sendrecv.entity.device.ModemEntity;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.*;
import static com.xy.netdev.common.util.ByteUtils.byteToInt;

/**
 * @author luo
 * @date 2021/3/30
 */
@Service
@Slf4j
public class CarAntennaImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData>{

    /**响应标识数组，用来校验响应帧结构*/
    private static final String[] RESPONSE_SIGNS = {"81","83","85"};

    @Autowired
    private DztCtrlInterPrtcServiceImpl ctrlInterService;
    @Autowired
    private DztQueryInterPrtcServiceImpl queryInterService;

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
        int len = bytesToNum(bytes, 4, 2, ByteBuf::readShort);
        //响应数据类型标识   查询0X53 控制0X41
        Byte respType = bytesToNum(bytes, 7, 1, ByteBuf::readByte);
        String hexRespType = lefPadNumToHexStr(Long.valueOf(respType));
        if (!Arrays.asList(RESPONSE_SIGNS).contains(hexRespType)){
            log.error("收到包含错误响应标识的帧结构，标识字节：{}----数据体：{}",hexRespType,bytes);
        }
        //接口命令标识
        Byte prtcCmd = bytesToNum(bytes, 7, 1, ByteBuf::readByte);
        //数据体
        byte[] paramBytes = byteArrayCopy(bytes, 8, len);
        String hexPrtcCmd = lefPadNumToHexStr(Long.valueOf(prtcCmd));

        //获取操作类型
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByPara(frameRespData.getDevType(), hexPrtcCmd);
        String operateType = BaseInfoContainer.getOptByPrtcl(prtclFormat, hexRespType);
        frameRespData.setOperType(operateType);
        frameRespData.setCmdMark(hexPrtcCmd);
        frameRespData.setParamBytes(paramBytes);
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        return new byte[0];
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
