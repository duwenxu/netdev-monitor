package com.xy.netdev.frame.service.impl;


import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.monitor.bo.DevInterParam;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.monitor.service.IInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.ArrayList;
import java.util.List;

/**
 * 39所Ku&L下变频器接口协议解析
 *
 * @author admin
 * @date 2021-03-05
 */
public class FreqConverterInterPrtcParser implements IQueryInterPrtclAnalysisService {

    @Autowired
    IInterfaceService interfaceService;


    @Override
    public void queryPara(BaseInfo devInfo, DevInterParam interParam) {
        StringBuilder sb = new StringBuilder();
        sb.append(FreqConverterPrtcParser.SEND_START_MARK).append(devInfo.getDevIpAddr()).append("/")
                .append(interParam.getDevInterface().getItfCode());
        String command = sb.toString();
    }

    @Override
    public List<ParaInfo> queryParaResponse(BaseInfo devInfo, DevInterParam interInfo,byte[] dataByte) {
        String respStr = new String(dataByte);
        int startIdx = respStr.indexOf("_");
        int endIdx = respStr.indexOf("\\n");
        String str = respStr.substring(startIdx+1,endIdx);
        String[] params = str.split("\\r");
        List<ParaInfo> paraInfos = new ArrayList<>();
        for (String param : params) {
            String cmdMark = param.split("_")[0];
            String value = param.split("_")[1];
            ParaInfo paraInfo = new ParaInfo();
            paraInfo.setNdpaCmdMark(cmdMark);
            paraInfo.setParaVal(value);
            paraInfo.setDevType(devInfo.getDevType());
            paraInfos.add(paraInfo);
        }
        return paraInfos;
    }

}
