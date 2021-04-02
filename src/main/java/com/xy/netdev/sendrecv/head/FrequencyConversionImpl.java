package com.xy.netdev.sendrecv.head;

import cn.hutool.core.util.StrUtil;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.sendrecv.entity.SocketEntity;
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
        frameRespData.setReciveOriginalData(StrUtil.str(frameRespData.getParamBytes(), Charset.defaultCharset()));
        if(iParaPrtclAnalysisService != null){
            iParaPrtclAnalysisService.queryParaResponse(frameRespData);
        }
        if(iQueryInterPrtclAnalysisService != null){
            iQueryInterPrtclAnalysisService.queryParaResponse(frameRespData);
        }
    }

    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
        String data = new String(socketEntity.getBytes(), Charset.defaultCharset());
        log.error("------------------origin data:"+data);
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
        String paramMark = StrUtil.sub(data, beginOffset, endOffset);
        frameRespData.setCmdMark(paramMark);
        frameRespData.setParamBytes(socketEntity.getBytes());
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        return frameReqData.getParamBytes();
    }

    @Override
    public String cmdMarkConvert(FrameRespData frameRespData) {
        //获取设备CMD信息, '/'为调制解调器特殊格式, 因为调制解调器cmd为字符串, 不能进行十六进制转换, 所以特殊区分
        if (!StrUtil.contains(frameRespData.getCmdMark(), '/') && !StrUtil.contains(frameRespData.getCmdMark(), '?')) {
            return Integer.toHexString(Integer.parseInt(frameRespData.getCmdMark(), 16));
        } else {
            return StrUtil.removeAll(frameRespData.getCmdMark(), '/','?');
        }
    }

    @Override
    protected void setSendOriginalData(FrameReqData frameReqData, byte[] bytes) {
        frameReqData.setSendOriginalData(StrUtil.str(bytes, Charset.defaultCharset()));
    }

    @Override
    protected void setReceiveOriginalData(FrameRespData frameRespData, byte[] bytes) {
        frameRespData.setReciveOriginalData(StrUtil.str(bytes, Charset.defaultCharset()));
    }
}
