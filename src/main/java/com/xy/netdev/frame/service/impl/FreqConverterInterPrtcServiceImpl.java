package com.xy.netdev.frame.service.impl;


import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.entity.TransportEntity;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
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
public class FreqConverterInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    SocketMutualService socketMutualService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(FreqConverterPrtcServiceImpl.SEND_START_MARK).append(reqInfo.getDevNo()).append("/")
                .append(reqInfo.getCmdMark());
        String command = sb.toString();
        TransportEntity transportEntity = new TransportEntity();
        BaseInfo baseInfo = null;
        transportEntity.setDevInfo(baseInfo);
        transportEntity.setParamMark(reqInfo.getCmdMark());
        transportEntity.setParamBytes(command.getBytes());
        socketMutualService.request(transportEntity, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(TransportEntity transportEntity) {
        String respStr = new String(transportEntity.getParamBytes());
        int startIdx = respStr.indexOf("_");
        int endIdx = respStr.indexOf("\\n");
        String str = respStr.substring(startIdx+1,endIdx);
        String[] params = str.split("\\r");
        FrameRespData respData = new FrameRespData();
        List<FrameParaData> frameParaList = new ArrayList<>();
        for (String param : params) {
            String cmdMark = param.split("_")[0];
            String value = param.split("_")[1];
            FrameParaData paraInfo = new FrameParaData();
            paraInfo.setDevNo(transportEntity.getDevInfo().getDevNo());
            paraInfo.setDevType(transportEntity.getDevInfo().getDevType());
            ParaInfo respParaDetail = null;
            paraInfo.setParaNo(respParaDetail.getNdpaNo());
            paraInfo.setParaVal(value);
            frameParaList.add(paraInfo);
        }
        ParaInfo reqParaDetail = null;
        respData.setFrameParaList(frameParaList);
        respData.setCmdMark(transportEntity.getParamMark());
        respData.setDevNo(transportEntity.getDevInfo().getDevNo());
        respData.setDevType(transportEntity.getDevInfo().getDevType());
        respData.setOperType(SysConfigConstant.OPREATE_QUERY_RESP);
        respData.setAccessType(reqParaDetail.getNdpaAccessRight());
        return respData;
    }


}
