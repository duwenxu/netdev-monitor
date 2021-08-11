package com.xy.netdev.frame.service.tkuka;

import cn.hutool.core.util.StrUtil;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;

/**
 * TKuka0.9CA监控设备
 * @author sunchao
 * @create 2021-07-05 10:00
 */
@Service
@Slf4j
public class TkukaCaParaPrtcServiceImpl implements IParaPrtclAnalysisService {

    @Autowired
    SocketMutualService socketMutualService;

    @Autowired
    IDataReceiveService dataReciveService;

    @Override
    public void queryPara(FrameReqData reqInfo) {}

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) { return null; }

    /**
     * 参数控制
     * @param  reqInfo   请求参数信息
     */
    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        byte[] bytes = new byte[114];
        //工作模式
        System.arraycopy(StrUtil.bytes(reqInfo.getCmdMark()),0,bytes,0,1);
        reqInfo.setParamBytes(bytes);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        String str = StrUtil.str(respData.getParamBytes(), Charset.defaultCharset());
        respData.setReciveOriginalData(str);
        dataReciveService.paraCtrRecive(respData);
        return respData;
    }
}
