package com.xy.netdev.frame.service.shortwave;

import cn.hutool.core.util.StrUtil;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.impl.DataReciveServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.listToBytes;

/**
 * @Desc
 * @Author 嗜雪的蚂蚁
 * @Date 2021/7/14 11:01
 **/
@Component
@Slf4j
public class ShortWaveParaServiceImpl implements IParaPrtclAnalysisService {
    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private DataReciveServiceImpl dataReciveService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
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
        //控制参数信息拼接
        FrameParaData paraData = paraList.get(0);
        String paraVal = paraData.getParaVal();
        byte[] dataBytes = null;
        if (!StringUtils.isBlank(paraVal)) {
            dataBytes = StrUtil.bytes(paraVal);
        }
        reqInfo.setParamBytes(dataBytes);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        return null;
    }
}
