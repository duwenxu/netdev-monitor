package com.xy.netdev.frame.service.impl.head;

import cn.hutool.core.util.StrUtil;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;

/**
 * 1:1下变频器
 * @author cc
 */
@Service
@Slf4j
public class FrequencyConversionImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {


    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService,
                         IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService) {
        if(iParaPrtclAnalysisService != null){
            iParaPrtclAnalysisService.ctrlParaResponse(frameRespData);
        }
        if(iQueryInterPrtclAnalysisService != null){
            iQueryInterPrtclAnalysisService.queryParaResponse(frameRespData);
        }
    }

    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
        String data = new String(socketEntity.getBytes(), Charset.defaultCharset());
        int beginOffset;
        char errorMark = '?';
        if (StrUtil.contains(data, errorMark)){
            beginOffset = StrUtil.indexOf( data, errorMark);
            frameRespData.setRespCode(SysConfigConstant.RPT_DEV_STATUS_ISALARM_YES);
        }else {
            beginOffset = StrUtil.indexOf( data, '/');
            frameRespData.setRespCode(SysConfigConstant.RPT_DEV_STATUS_ISALARM_NO);
        }
        int endOffset = StrUtil.indexOf(data, '_');
        String paramMark = StrUtil.sub(data, beginOffset+1, endOffset);
        frameRespData.setCmdMark(paramMark);
        frameRespData.setParamBytes(socketEntity.getBytes());
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        return frameReqData.getParamBytes();
    }
}
