package com.xy.netdev.frame.service.newacu;

import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.modemscmm.ModemScmmPrtcServiceImpl;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.listToBytes;

/**
 * 7.3mACU接口控制实现
 *
 * @author duwenxu
 * @create 2021-08-09 14:17
 */
@Component
@Slf4j
public class AcuNewCtrlServiceImpl implements ICtrlInterPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private ModemScmmPrtcServiceImpl modemScmmPrtcService;

    @Override
    public void ctrlPara(FrameReqData reqData) {
        List<FrameParaData> paraList = reqData.getFrameParaList();
        if (paraList == null || paraList.isEmpty()) { return; }
        List<byte[]> ctlParamList = new ArrayList<>();
        /**组装控制参数bytes*/
        for (FrameParaData paraData : paraList) {
            byte[] frameBytes = modemScmmPrtcService.doGetFrameBytes(paraData);
            ctlParamList.add(frameBytes);
        }
        byte[] paramBytes = listToBytes(ctlParamList);
        reqData.setParamBytes(paramBytes);
        socketMutualService.request(reqData, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        return null;
    }
}
