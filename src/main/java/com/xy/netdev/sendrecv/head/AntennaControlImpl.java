package com.xy.netdev.sendrecv.head;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import com.xy.netdev.sendrecv.entity.device.AntennaControlEntity;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.transit.IDataReciveService;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.bytesToNum;
import static com.xy.netdev.common.util.ByteUtils.listToBytes;

/**
 * 40w功放
 * @author cc
 */
@Service
@Slf4j
public class AntennaControlImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {

    @Autowired
    IDataReciveService dataReciveService;

    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService,
                         IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService, ICtrlInterPrtclAnalysisService ctrlInterPrtclAnalysisService) {
        if (iParaPrtclAnalysisService != null){
            iParaPrtclAnalysisService.ctrlParaResponse(frameRespData);
            dataReciveService.paraCtrRecive(frameRespData);
            return;
        }
        iQueryInterPrtclAnalysisService.queryParaResponse(frameRespData);
        dataReciveService.paraQueryRecive(frameRespData);
    }

    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
        byte[] bytes = socketEntity.getBytes();
        //数据体长度
        Byte length = bytesToNum(bytes, 1, 1, ByteBuf::readByte);
        //命令
        Byte cmd = bytesToNum(bytes, 3, 1, ByteBuf::readByte);

        //
        Byte respCode = bytesToNum(bytes, 4, 1, ByteBuf::readByte);
        //数据体
        byte[] paramData = ByteUtils.byteArrayCopy(bytes, 4, length  - 6);
        frameRespData.setCmdMark(Integer.toHexString(cmd));
        frameRespData.setParamBytes(paramData);
        frameRespData.setRespCode(respCode.toString());
        bytesToNum(bytes, 4, 1, ByteBuf::readByte);

        //目的地址
//        Byte sad = bytesToNum(originalReceiveBytes, 2, 1, ByteBuf::readByte);
        //校验字
//        byte check = bytesToNum(originalReceiveBytes, originalReceiveBytes.length - 2, 1, ByteBuf::readByte);
//        AntennaControlEntity antennaControlEntity = AntennaControlEntity.builder()
//                .lc(length)
//                .sad(sad)
//                .cmd(cmd)
//                .data(paramData)
//                .build();
//        byte vs = xor(antennaControlEntity);
//        if (vs != check){
//            log.warn("40公放校验字不匹配！！！, 期待值：{}， 实际值：{}", vs, check);
//        }
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        //参数数据
        byte[] paramBytes = frameReqData.getParamBytes();
        //数据长度,初始长度为6
        int dataLength = 6;
        if (paramBytes != null){
            dataLength += paramBytes.length;
        }
        AntennaControlEntity antennaControlEntity = AntennaControlEntity.builder()
                .stx((byte) 0x7B)
                .lc((byte) dataLength)
                .sad((byte) 0)
                .cmd(Byte.valueOf(frameReqData.getCmdMark(), 16))
                .data(paramBytes)
                .vs((byte) 0)
                .etx((byte) 0x7D)
                .build();
        byte vs = xor(antennaControlEntity);
        antennaControlEntity.setVs(vs);
        byte[] pack = pack(antennaControlEntity);
        log.debug("40W功放发送查询/控制帧内容：{}", HexUtil.encodeHexStr(pack));
        return pack;
    }


    private byte xor(AntennaControlEntity entity){
        byte[] bytes = {entity.getLc(), entity.getSad(), entity.getCmd()};
        byte[] xorBytes = ArrayUtil.addAll(bytes, entity.getData());
        return ByteUtils.xor(xorBytes, 0, xorBytes.length);
    }

    private byte[] pack(AntennaControlEntity antennaControlEntity){
        List<byte[]> list = new ArrayList<>();
        list.add(new byte[]{antennaControlEntity.getStx()});
        list.add(new byte[]{antennaControlEntity.getLc()});
        list.add(new byte[]{antennaControlEntity.getSad()});
        list.add(new byte[]{antennaControlEntity.getCmd()});
        if (antennaControlEntity.getData() != null){
            list.add(antennaControlEntity.getData());
        }
        list.add(new byte[]{antennaControlEntity.getVs()});
        list.add(new byte[]{antennaControlEntity.getEtx()});
        return listToBytes(list);
    }

    @Override
    public String cmdMarkConvert(FrameRespData frameRespData) {
        //获取设备CMD信息, '/'为调制解调器特殊格式, 因为调制解调器cmd为字符串, 不能进行十六进制转换, 所以特殊区分
        if (!StrUtil.contains(frameRespData.getCmdMark(), '/')){
            return Integer.toHexString(Integer.parseInt(frameRespData.getCmdMark(),16));
        }else {
            return  StrUtil.removeAll(frameRespData.getCmdMark(), '/');
        }
    }
}
