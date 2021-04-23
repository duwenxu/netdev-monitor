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
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.PrtclFormat;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.sendrecv.entity.device.CarAntennaEntity;
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
 * @author luo
 * @date 2021/3/30
 */
@Service
@Slf4j
public class CarAntennaImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData>{

    /**响应标识数组，用来校验响应帧结构*/
    private static final String[]   RESPONSE_SIGNS = {"83","85","81"};

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
        byte[] bytes = byteReplace(socketEntity.getBytes(), 1, socketEntity.getBytes().length - 1, Pair.of("7D5E", "7E"), Pair.of("7D5D", "7D"));
        //数据体长度
        int len = bytesToNum(bytes, 5, 2, ByteBuf::readShort);
        //响应数据类型标识   查询0X53 控制0X41
        Byte respType = bytesToNum(bytes, 7, 1, ByteBuf::readByte);
        String hexRespType = HexUtil.toHex(Integer.valueOf(respType));
        String hexPrtcCmd = hexRespType.substring(hexRespType.length()-2);
        if (!Arrays.asList(RESPONSE_SIGNS).contains(hexPrtcCmd)){
            log.error("收到包含错误响应标识的帧结构，标识字节：{}----数据体：{}",hexRespType,bytes);
        }
        //数据体
        byte[] paramBytes = byteArrayCopy(bytes, 8, len-1);
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(frameRespData.getDevType(), hexPrtcCmd);
        String operateType = BaseInfoContainer.getOptByPrtcl(prtclFormat, hexPrtcCmd);
        frameRespData.setOperType(operateType);
        frameRespData.setCmdMark(hexPrtcCmd);
        frameRespData.setParamBytes(paramBytes);
        return frameRespData;
    }


    @Override
    public byte[] pack(FrameReqData frameReqData) {
        byte[] paramBytes = frameReqData.getParamBytes();
        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterface(frameReqData.getDevType(), frameReqData.getCmdMark());
        String keyword;
        if (StrUtil.isNotBlank(prtclFormat.getFmtSkey())){
            keyword = prtclFormat.getFmtSkey();
        }else {
            keyword = prtclFormat.getFmtCkey();
        }
        int length = 0;
        if(paramBytes!=null){
            length = paramBytes.length;
        }
        CarAntennaEntity carAntennaEntity = CarAntennaEntity.builder()
                .beginOffset((byte) 0x7E)
                .deviceType((byte) 0x13)
                .devSubType(Byte.valueOf(getDevModel(frameReqData),16))
                .deviceAddress(new byte[]{(byte) 0x00,(byte) 0x01})
                .length(objToBytes(length+1, 2))
                .cmd((byte) Integer.parseInt(keyword, 16))
                .params(paramBytes)
                .end((byte)0x7E)
                .build();
        //累加校验和
        byte check = addGetBottom(carAntennaEntity);
        carAntennaEntity.setCheck(check);
        return pack(carAntennaEntity);
    }

    private byte[] pack(CarAntennaEntity carAntennaEntity){
        List<byte[]> list = new ArrayList<>();
        list.add(new byte[]{carAntennaEntity.getBeginOffset()});
        list.add(new byte[]{carAntennaEntity.getDeviceType()});
        list.add(new byte[]{carAntennaEntity.getDevSubType()});
        list.add(carAntennaEntity.getDeviceAddress());
        list.add(carAntennaEntity.getLength());
        list.add(new byte[]{carAntennaEntity.getCmd()});
        if (carAntennaEntity.getParams() != null && carAntennaEntity.getParams().length > 0){
            list.add(carAntennaEntity.getParams());
        }
        list.add(new byte[]{carAntennaEntity.getCheck()});
        list.add(new byte[]{carAntennaEntity.getEnd()});
        byte[] bytes = listToBytes(list);
        return byteReplace(bytes, 1, bytes.length - 1, Pair.of("7E", "7D5E"), Pair.of("7D", "7D5D"));

    }

    /**
     * 累加从 长度 到 参数体 的所有内容作为校验和
     *
     * @param entity 数据体
     * @return 校验和字节
     */
    private byte addGetBottom(CarAntennaEntity entity) {
        List<byte[]> list = new ArrayList<>();
        list.add(entity.getDeviceAddress());
        list.add(entity.getLength());
        list.add(new byte[]{entity.getCmd()});
        if (entity.getParams() != null && entity.getParams().length > 0){
            list.add(entity.getParams());
        }
        byte[] bytes = listToBytes(list);
        return ByteUtils.addGetBottom(bytes, 0, bytes.length);
    }

    /**
     * 累加取模
     * @param values 数据值
     * @return 校验位
     */
    private static byte addDiv(int... values){
        double div = (Arrays.stream(values).sum()) % 256;
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

    /**
     * 获取子设备型号 编码
     * @param frameReqData 数据结构
     * @return 子设备编码
     */
    private String getDevModel(FrameReqData frameReqData) {
        BaseInfo baseInfo = BaseInfoContainer.getDevInfoByNo(frameReqData.getDevNo());
        String devSubType = baseInfo.getDevSubType();
        if (devSubType!=null){
            return sysParamService.getParaRemark1(devSubType);
        }
        return null;
    }


    public static void main(String[] args) {
        byte[] bytes = new byte[]{(byte)0x00,(byte)0x03,(byte)0x81,(byte)0x30,(byte)0x20};
        byte b = ByteUtils.addGetBottom(bytes, 0, bytes.length);
        System.out.println(byteToBinary(b));
    }
}
