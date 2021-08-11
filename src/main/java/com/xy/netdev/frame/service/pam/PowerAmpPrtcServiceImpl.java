package com.xy.netdev.frame.service.pam;

import cn.hutool.core.codec.BCD;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReceiveService;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.*;
import static com.xy.netdev.monitor.constant.MonitorConstants.BYTE;
import static com.xy.netdev.monitor.constant.MonitorConstants.STR;

/**
 * Ku400w功放 参数查询响应 帧协议解析层
 *
 * @author duwenxu
 * @create 2021-03-22 15:30
 */
@Service
@Slf4j
public class PowerAmpPrtcServiceImpl implements IParaPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReceiveService dataReciveService;
    /**
     * 分隔符 ASCII码为 0x2C
     */
    private static final String SPLIT = ",";

    @Override
    public void queryPara(FrameReqData reqInfo) {
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        return null;
    }

    /**
     * Ku400w功放 参数控制
     *
     * @param reqInfo 请求参数信息
     */
    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        List<FrameParaData> paraList = reqInfo.getFrameParaList();
        if (paraList == null || paraList.isEmpty()) {
            return;
        }
        FrameParaData paraData = paraList.get(0);
        FrameParaInfo paraInfoByNo = BaseInfoContainer.getParaInfoByNo(paraData.getDevType(), paraData.getParaNo());
        String dataType = paraInfoByNo.getDataType();
        byte[] bytes = HexUtil.decodeHex(reqInfo.getCmdMark());
        //此处只有 衰减可设置
        switch (dataType) {
            case STR:
                byte[] valBytes = StrUtil.bytes(paraData.getParaVal());
                bytes = bytesMerge(bytes, valBytes);
                break;
            case BYTE:
                //功放开关设置 直接发送状态位 80关/81开
                String byteVal = "0".equals(paraData.getParaVal()) ? "80" : "81";
                String dataBody = reqInfo.getCmdMark() + byteVal;
                bytes = HexUtil.decodeHex(dataBody);
                break;
            default:
                break;
        }
        reqInfo.setParamBytes(bytes);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    /**
     * Ku400w功放 参数控制响应
     *
     * @param respData 协议解析响应数据
     * @return 响应结果
     */
    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        byte[] paramBytes = respData.getParamBytes();
        //参数标识
        Byte cmd = bytesToNum(paramBytes, 0, 1, ByteBuf::readByte);
        String hexCmd = numToHexStr(Long.valueOf(cmd));
        //响应标识
        Byte response = bytesToNum(paramBytes, 1, 1, ByteBuf::readByte);
        String hexResponse = numToHexStr(Long.valueOf(response));
        respData.setCmdMark(hexCmd);
        respData.setRespCode(hexResponse);
        dataReciveService.paraCtrRecive(respData);
        return respData;
    }

    public static void main(String[] args) {
//        byte[] bytes = {0x56, 0x32, 0x2E, 0x30, 0x2D, 0x31, 0x39, 0x30, 0x39, 0x32, 0x33};
        byte[] bytes = {0x02, 0x05};
//        String str = StrUtil.str(bytes, Charsets.UTF_8);
//        String str = byteToBinary((byte) 0x50);
        String s = "0205";
        String str = BCD.bcdToStr(bytes);
//        String str = ParamDecoder.BCD(bytes, 2);
        System.out.println(BCD.strToBcd(s));
    }
}



