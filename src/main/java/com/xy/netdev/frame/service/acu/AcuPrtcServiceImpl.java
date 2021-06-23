package com.xy.netdev.frame.service.acu;


import cn.hutool.core.util.StrUtil;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.transit.IDataReciveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;


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

    @Autowired
    IDataReciveService dataReciveService;


    @Override
    public void queryPara(FrameReqData reqInfo) {

    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        return null;
    }

    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        String paraVal = reqInfo.getFrameParaList().get(0).getParaVal();
        String replace = paraVal.replace("{", "")
                .replace("}","")
                .replace("[","")
                .replace("]","");
        String command = "<" + reqInfo.getCmdMark() + replace + ">";
        reqInfo.setParamBytes(StrUtil.bytes(command));
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        String str = StrUtil.str(respData.getParamBytes(), Charset.defaultCharset());
        respData.setReciveOriginalData(str);
        dataReciveService.paraCtrRecive(respData);
        return respData;
    }
}
