package com.xy.netdev.sendrecv.head;

import cn.hutool.core.util.StrUtil;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 华达电源监控
 */

@Service
@Slf4j
public class HdpmImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {

    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService,
                         IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService, ICtrlInterPrtclAnalysisService ctrlInterPrtclAnalysisService) {
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
        String paramMark = "";
        if(endOffset>beginOffset){
            if(data.contains("/CH")){
                paramMark = StrUtil.sub(data, beginOffset, data.indexOf(":")).replace("_0","");
            }else{
                paramMark= StrUtil.sub(data, beginOffset, endOffset);
            }
            frameRespData.setCmdMark(paramMark);
            frameRespData.setParamBytes(socketEntity.getBytes());
            frameRespData.setAccessType(getAccessType(frameRespData.getDevType(),paramMark.substring(1)));
        }else{
            log.error("华达电源监控返回数据异常：{}",data);
        }

        return frameRespData;
    }

    private String getAccessType(String devType,String cmdMark){
        String accessType = "";
        FrameParaInfo frameParaInfo =  BaseInfoContainer.getParaInfoByCmd(devType,cmdMark);
        if(frameParaInfo != null){
            accessType =  SysConfigConstant.ACCESS_TYPE_PARAM;
        }
        Interface interf =  BaseInfoContainer.getInterLinkInterface(devType,cmdMark);
        if(interf.getItfId() != null){
            accessType =  SysConfigConstant.ACCESS_TYPE_INTERF;
        }
        return accessType;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        byte[] paramBytes = frameReqData.getParamBytes();
        log.debug("华达电源监控发送查询/控制帧：[{}]",StrUtil.str(paramBytes, StandardCharsets.UTF_8));
        return paramBytes;
    }

    @Override
    public String cmdMarkConvert(FrameRespData frameRespData) {
        String cmdMark = "";
        if(StringUtils.isNotEmpty(frameRespData.getCmdMark())){
            //获取设备CMD信息, '/'为调制解调器特殊格式, 因为调制解调器cmd为字符串, 不能进行十六进制转换, 所以特殊区分
            if (!StrUtil.contains(frameRespData.getCmdMark(), '/') && !StrUtil.contains(frameRespData.getCmdMark(), '?')) {
                cmdMark =  frameRespData.getCmdMark();
            } else {
                cmdMark =  StrUtil.removeAll(frameRespData.getCmdMark(), '/','?');
            }
        }
        return cmdMark;
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
