package com.xy.netdev.frame.service.modemscmm;

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
import com.xy.netdev.transit.IDataReciveService;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.*;
import static com.xy.netdev.monitor.constant.MonitorConstants.BYTE;
import static com.xy.netdev.monitor.constant.MonitorConstants.STR;

/**
 * SCMM-2300调制解调器 参数协议内容解析
 *
 * @author duwenxu
 * @create 2021-03-30 13:51
 */
@Service
@Slf4j
public class ModemScmmPrtcServiceImpl implements IParaPrtclAnalysisService {
    @Autowired
    private IDataReciveService dataReciveService;
    @Autowired
    private SocketMutualService socketMutualService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        return null;
    }

    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        List<FrameParaData> paraList = reqInfo.getFrameParaList();
        if (paraList == null || paraList.isEmpty()) { return; }
        //控制参数信息拼接
        FrameParaData paraData = paraList.get(0);
        FrameParaInfo paraInfoByNo = BaseInfoContainer.getParaInfoByNo(paraData.getDevType(), paraData.getParaNo());
        String dataType = paraInfoByNo.getDataType();
        byte[] bytes = HexUtil.decodeHex(reqInfo.getCmdMark());
        //按数据类型处理
        switch (dataType){
            case STR:
                byte[] valBytes = StrUtil.bytes(paraData.getParaVal());
                bytes = bytesMerge(bytes,valBytes); break;
            case BYTE:
                String byteVal = "0".equals(paraData.getParaVal())? "80":"81";
                String dataBody = reqInfo.getCmdMark() + byteVal;
                bytes = HexUtil.decodeHex(dataBody);
                break;
            default:
                break;
        }
        reqInfo.setParamBytes(bytes);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        byte[] paramBytes = respData.getParamBytes();
        //单元标识
        Byte unit = bytesToNum(paramBytes, 0, 1, ByteBuf::readByte);
        //参数关键字
        Byte cmd = bytesToNum(paramBytes, 1, 1, ByteBuf::readByte);
        //设置响应
        Byte res = bytesToNum(paramBytes, 2, 1, ByteBuf::readByte);
        respData.setCmdMark(numToHexStr(Long.valueOf(cmd)));
        respData.setRespCode(numToHexStr(Long.valueOf(res)));
        dataReciveService.paraCtrRecive(respData);
        return respData;
    }
}
