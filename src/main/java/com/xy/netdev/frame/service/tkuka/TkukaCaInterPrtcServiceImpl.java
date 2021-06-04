package com.xy.netdev.frame.service.tkuka;

import cn.hutool.core.util.HexUtil;
import com.xy.common.exception.BaseException;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.common.util.SpringContextUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.ParamCodec;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.xy.netdev.common.constant.SysConfigConstant.PARA_DATA_TYPE_INT;
import static com.xy.netdev.common.constant.SysConfigConstant.PARA_DATA_TYPE_STR;

/**
 * TKuka0.9CA监控设备
 *
 * @author sunchao
 * @create 2021-05-31 14:00
 */
@Service
@Slf4j
public class TkukaCaInterPrtcServiceImpl implements ICtrlInterPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;

    @Override
    public void ctrlPara(FrameReqData reqData) {
        List<FrameParaData> paraList = reqData.getFrameParaList();
        byte[] bytes = new byte[13];
        for (FrameParaData paraData : paraList) {
            FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByNo(paraData.getDevType(),paraData.getParaNo());
            if(PARA_DATA_TYPE_STR.equals(frameParaInfo.getDataType())){
                String paraVal = StringUtils.leftPad(paraData.getParaVal(),Integer.valueOf(frameParaInfo.getParaByteLen())*2-paraData.getParaVal().length(),"0");
                frameParaInfo.setParaVal(paraVal);
            }else{
                ParamCodec handler = SpringContextUtils.getBean(frameParaInfo.getNdpaRemark2Data());
                String paraValStr = HexUtil.encodeHexStr(handler.encode(paraData.getParaVal(), frameParaInfo.getNdpaRemark1Data()));
                frameParaInfo.setParaVal(paraValStr);
            }
            byte[] frameBytes = HexUtil.decodeHex(frameParaInfo.getParaVal());
            bytes = ByteUtils.bytesMerge(bytes, frameBytes);
        }
        reqData.setParamBytes(bytes);
        log.info("TKuka0.9CA监控设备发送控制帧标识字：[{}]，内容：[{}]",reqData.getCmdMark(), HexUtil.encodeHexStr(bytes));
        socketMutualService.request(reqData, ProtocolRequestEnum.CONTROL);
    }


    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        return null;
    }
}
