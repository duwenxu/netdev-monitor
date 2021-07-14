package com.xy.netdev.frame.service.tkuka;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
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
        byte[] bytes = new byte[114];
        //工作模式
        System.arraycopy(StrUtil.bytes(reqData.getCmdMark()),0,bytes,0,1);
        int num = 0;
        for (FrameParaData paraData : paraList) {
            FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByNo(paraData.getDevType(),paraData.getParaNo());
            //参数值
            if(StringUtils.isNotBlank(frameParaInfo.getNdpaRemark3Data())){
                num = Integer.valueOf(frameParaInfo.getNdpaRemark3Data());
            }
            System.arraycopy(getBytes(frameParaInfo,paraData.getParaVal()),0,bytes,num,paraData.getLen());
            num = num + Integer.valueOf(frameParaInfo.getParaByteLen());
        }
        reqData.setParamBytes(bytes);
        log.info("TKuka0.9CA监控设备发送控制帧标识字：[{}]，内容：[{}]",reqData.getCmdMark(), HexUtil.encodeHexStr(bytes));
        socketMutualService.request(reqData, ProtocolRequestEnum.CONTROL);
    }


    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        return null;
    }


    private byte[] getBytes(FrameParaInfo param, String value) {
        byte[] bytes = null;
        if (StringUtils.isNotBlank(param.getNdpaRemark2Data())) {
            ParamCodec handler = SpringContextUtils.getBean(param.getNdpaRemark2Data());
            bytes = handler.encode(value,null);
        } else {
            bytes = HexUtil.decodeHex(value);
        }
        return bytes;
    }
}
