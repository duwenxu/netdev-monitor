package com.xy.netdev.frame.service.gf;

import cn.hutool.core.bean.BeanUtil;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import static com.xy.netdev.common.util.ByteUtils.byteToNumber;
import static com.xy.netdev.frame.service.gf.GfPrtcServiceImpl.isUnsigned;

/**
 * 40W功放接口协议解析
 * @author cc
 */
@Service
public class GfInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    SocketMutualService socketMutualService;

    @Autowired
    ISysParamService sysParamService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        reqInfo.setAccessType(SysConfigConstant.ACCESS_TYPE_PARAM);
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
}