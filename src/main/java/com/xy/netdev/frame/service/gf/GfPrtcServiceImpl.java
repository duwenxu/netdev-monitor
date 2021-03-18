package com.xy.netdev.frame.service.gf;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xy.netdev.common.util.ByteUtils.byteToNumber;
import static com.xy.netdev.frame.service.gf.GfInterPrtcServiceImpl.setParamBytes;

/**
 * 40W 功放
 * @author cc
 */
@Service
@Slf4j
public class GfPrtcServiceImpl implements IParaPrtclAnalysisService {

    @Autowired
    SocketMutualService socketMutualService;

    @Autowired
    ISysParamService sysParamService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        setParamBytes(reqInfo);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),respData.getCmdMark());
        byte[] bytes = respData.getParamBytes();
        FrameParaData paraInfo = new FrameParaData();
        BeanUtil.copyProperties(frameParaInfo, paraInfo, true);
        BeanUtil.copyProperties(respData, paraInfo, true);
        paraInfo.setLen(Integer.parseInt(frameParaInfo.getParaByteLen()));
        paraInfo.setParaVal(byteToNumber(bytes, frameParaInfo.getParaStartPoint() - 1,
                Integer.parseInt(frameParaInfo.getParaByteLen()), isUnsigned(sysParamService, frameParaInfo.getAlertPara())).toString());
        respData.setFrameParaList(Lists.list(paraInfo));
        return respData;
    }

    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        setParamBytes(reqInfo);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }
    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),respData.getCmdMark());
        byte[] bytes = respData.getParamBytes();
        FrameParaData paraInfo = new FrameParaData();
        BeanUtil.copyProperties(frameParaInfo, paraInfo, true);
        BeanUtil.copyProperties(respData, paraInfo, true);
        paraInfo.setLen(Integer.parseInt(frameParaInfo.getParaByteLen()));
        paraInfo.setParaVal(byteToNumber(bytes, 0,
                Integer.parseInt(frameParaInfo.getParaByteLen()), isUnsigned(sysParamService, frameParaInfo.getAlertPara())).toString());
        respData.setFrameParaList(Lists.list(paraInfo));
        return respData;
    }

    public static boolean isUnsigned(ISysParamService sysParamService, String paraNo){
        String isUnsigned = sysParamService.getParaRemark1(paraNo);
        if (StrUtil.isBlank(isUnsigned)){
            return true;
        }
        return Integer.parseInt(isUnsigned) == 1;
    }


}
