package com.xy.netdev.frame.service.pam;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.Charsets;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.bytesToNum;
import static com.xy.netdev.common.util.ByteUtils.numToHexStr;

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
    /**分隔符 ASCII码为 0x2C*/
    private static final String SPLIT=",";

    @Override
    public void queryPara(FrameReqData reqInfo) {
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        return null;
    }

    /**
     * Ku400w功放 参数控制
     * @param  reqInfo   请求参数信息
     */
    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        List<FrameParaData> paraList = reqInfo.getFrameParaList();
        if (paraList == null|| paraList.isEmpty()){return;}
        String paraVal = paraList.get(0).getParaVal();
        String dataBody = reqInfo.getCmdMark() + paraVal;
        byte[] bytes = HexUtil.decodeHex(dataBody);
        reqInfo.setParamBytes(bytes);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    /**
     * Ku400w功放 参数控制响应
     * @param  respData   协议解析响应数据
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
        return respData;
    }


    public static void main(String[] args) {
        byte[] bytes = {0x32, 0x30, 0x2E, 0x30, 0x30};
        String str = StrUtil.str(bytes, Charsets.UTF_8);
        System.out.println(str);
    }
}



