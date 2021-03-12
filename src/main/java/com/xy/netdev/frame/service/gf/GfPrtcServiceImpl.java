package com.xy.netdev.frame.service.gf;

import cn.hutool.core.bean.BeanUtil;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xy.netdev.common.util.ByteUtils.byteToNumber;

/**
 * 40W 功放
 * @author cc
 */
@Service
public class GfPrtcServiceImpl implements IParaPrtclAnalysisService {

    @Autowired
    SocketMutualService socketMutualService;

    @Autowired
    ISysParamService sysParamService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        FrameParaInfo frameParaInfo = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(),respData.getCmdMark());
        byte[] bytes = respData.getParamBytes();
        FrameParaData paraInfo = new FrameParaData();
        BeanUtil.copyProperties(frameParaInfo, paraInfo, true);
        paraInfo.setParaVal(byteToNumber(bytes, frameParaInfo.getParaStartPoint(),
                Integer.parseInt(frameParaInfo.getParaByteLen()), isUnsigned(sysParamService, frameParaInfo.getParaNo())).toString());
        return respData;
    }

    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        return this.queryParaResponse(respData);
    }

    public static boolean isUnsigned(ISysParamService sysParamService, String paraNo){
        String isUnsigned = sysParamService.getParaRemark1(paraNo);
        return Integer.parseInt(isUnsigned) == 1;
    }
}
