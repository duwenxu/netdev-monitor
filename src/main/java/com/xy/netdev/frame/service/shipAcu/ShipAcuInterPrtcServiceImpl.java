package com.xy.netdev.frame.service.shipAcu;

import cn.hutool.core.util.HexUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 1.5米ACU天线控制实现(船载)
 *
 * @author sunchao
 * @create 2021-05-16 11:08
 */
@Service
@Slf4j
public class ShipAcuInterPrtcServiceImpl implements ICtrlInterPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;

    @Override
    public void ctrlPara(FrameReqData reqData) {
        List<FrameParaData> paraList = reqData.getFrameParaList();
        byte[] bytes = new byte[]{};
        for (FrameParaData paraData : paraList) {
            String paraValStr = "";
            if(paraData.getLen()==1){
                paraValStr = BitToHexStr(paraData.getParaVal().replaceAll("[^0-9]",""));
            }else{
                paraValStr = HexUtil.encodeHexStr(ByteUtils.objToBytes(paraData.getParaVal(),paraData.getLen()));
            }
            paraData.setParaVal(paraValStr);
            byte[] frameBytes = HexUtil.decodeHex(paraValStr);
            bytes = ByteUtils.bytesMerge(bytes, frameBytes);
        }
        reqData.setParamBytes(bytes);
        log.info("1.5米ACU天线发送控制帧标识字：[{}]，内容：[{}]",reqData.getCmdMark(), HexUtil.encodeHexStr(bytes));
        socketMutualService.request(reqData, ProtocolRequestEnum.CONTROL);
    }


    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        return null;
    }

    /**
     * Bit转Byte
     */
    private String BitToHexStr(String byteStr) {
        int re, len;
        if (null == byteStr) {
            throw new BaseException("比特位长度异常，请检查");
        }
        len = byteStr.length();
        if (len<8) {
            byteStr = StringUtils.leftPad(byteStr,8-len,"0");
        }
        if (len == 8) {// 8 bit处理
            if (byteStr.charAt(0) == '0') {// 正数
                re = Integer.parseInt(byteStr, 2);
            } else {// 负数
                re = Integer.parseInt(byteStr, 2) - 256;
            }
        } else {//4 bit处理
            re = Integer.parseInt(byteStr, 2);
        }
        return HexUtil.encodeHexStr(new byte[]{(byte) re}) ;
    }
}
