package com.xy.netdev.frame.service.acu;


import cn.hutool.core.bean.BeanUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.BaseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * ACU卫通天线接口协议解析
 *
 * @author luo
 * @date 2021-03-05
 */
@Component
public class AcuPrtcServiceImpl implements IParaPrtclAnalysisService {

    @Autowired
    SocketMutualService socketMutualService;


    @Override
    public void queryPara(FrameReqData reqInfo) {

    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        return null;
    }

    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(reqInfo.getCmdMark()).append(">")
                .append("_").append(reqInfo.getFrameParaList().get(0).getParaVal());


        String command = sb.toString();
        reqInfo.setParamBytes(command.getBytes());
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {

        return respData;
    }
}
