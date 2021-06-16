package com.xy.netdev.frame.service.shortwave;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.modemscmm.ModemScmmPrtcServiceImpl;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.impl.DataReciveServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.bytesMerge;


/**
 * 750-400W短波设备(接口参数查询)
 */
@Slf4j
@Component
public class ShortWaveInterPrtcServiceImpl implements IParaPrtclAnalysisService {
    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private DataReciveServiceImpl dataReciveService;
    @Autowired
    private ModemScmmPrtcServiceImpl modemScmmPrtcService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        return null;
    }

    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        List<FrameParaData> paraList = reqInfo.getFrameParaList();
        if (paraList == null || paraList.isEmpty()) {
            return;
        }
        FrameParaData paraData = paraList.get(0);
        byte[] frameBytes = modemScmmPrtcService.doGetFrameBytes(paraData);
        byte[] bytes = HexUtil.decodeHex(reqInfo.getCmdMark());
        byte[] bytesMerge = bytesMerge(bytes, frameBytes);
        reqInfo.setParamBytes(bytesMerge);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        return null;
    }
}
