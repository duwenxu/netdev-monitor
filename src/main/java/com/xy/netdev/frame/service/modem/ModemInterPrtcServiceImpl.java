package com.xy.netdev.frame.service.modem;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.xy.netdev.common.util.ByteUtils.byteToNumber;
import static com.xy.netdev.frame.service.gf.GfPrtcServiceImpl.isUnsigned;

/**
 * @author duwenxu
 * @create 2021-03-11 14:26
 */
@Slf4j
@Component
public class ModemInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    SocketMutualService socketMutualService;

    @Autowired
    ISysParamService sysParamService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        reqInfo.setAccessType(SysConfigConstant.ACCESS_TYPE_INTERF);
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
