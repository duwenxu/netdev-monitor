package com.xy.netdev.frame.service.impl.head;

import cn.hutool.core.util.StrUtil;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.service.bpq.BpqPrtcServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.charset.Charset;

/**
 * 1:1下变频器
 * @author cc
 */
@Service
@Slf4j
public class FrequencyConversionImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {

    @Resource
    protected BpqPrtcServiceImpl bpqPrtcService;

    @Override
    public void callback(FrameRespData frameRespData) {
        bpqPrtcService.ctrlParaResponse(frameRespData);
    }

    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
        String data = new String(socketEntity.getBytes(), Charset.defaultCharset());
        int beginOffset;
        char errorMark = '?';
        if (StrUtil.contains(data, errorMark)){
            beginOffset = StrUtil.indexOf( data, errorMark);
        }else {
            beginOffset = StrUtil.indexOf( data, '/');
        }
        int endOffset = StrUtil.indexOf(data, '_');
        String paramMark = StrUtil.sub(data, beginOffset, endOffset);
        frameRespData.setCmdMark(paramMark);
        frameRespData.setParamBytes(socketEntity.getBytes());
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        return frameReqData.getParamBytes();
    }
}
