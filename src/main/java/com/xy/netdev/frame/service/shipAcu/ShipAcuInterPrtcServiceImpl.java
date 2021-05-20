package com.xy.netdev.frame.service.shipAcu;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 1.5米ACU天线控制实现(船载)
 *
 * @author sunchao
 * @create 2021-05-16 11:08
 */
@Service
@Slf4j
public class ShipAcuInterPrtcServiceImpl implements ICtrlInterPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;

    @Override
    public void ctrlPara(FrameReqData reqData) {
        List<FrameParaData> paraList = reqData.getFrameParaList();
        byte[] bytes = new byte[]{};
        for (FrameParaData paraData : paraList) {
            byte[] frameBytes = HexUtil.decodeHex(paraData.getParaVal());
            bytes = ByteUtils.bytesMerge(bytes, frameBytes);
        }
        reqData.setParamBytes(bytes);
        log.info("1.5米ACU天线发送控制帧标识字：[{}]，内容：[{}]",reqData.getCmdMark(), HexUtil.encodeHexStr(bytes));
        socketMutualService.request(reqData, ProtocolRequestEnum.CONTROL);
    }


    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        return null;
    }
}
