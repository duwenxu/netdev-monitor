package com.xy.netdev.frame.service.shortwr;

import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.modem.ModemInterPrtcServiceImpl;
import com.xy.netdev.frame.service.modemscmm.ModemScmmPrtcServiceImpl;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.common.constant.SysConfigConstant.READ_WRITE;

/**
 * 712短波设备  单参数查询设置协议实现类
 */
@Component
@Slf4j
public class ShortWaveRadioPrtcServiceImpl implements IParaPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReceiveService dataReciveService;
    @Autowired
    private ModemInterPrtcServiceImpl modemInterPrtcService;
    @Autowired
    private ModemScmmPrtcServiceImpl modemScmmPrtcService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        byte[] paramBytes = respData.getParamBytes();
        String cmdMark = respData.getCmdMark();
        List<FrameParaData> frameParaDataList = new ArrayList<>();

        FrameParaInfo paraInfoByCmd = BaseInfoContainer.getParaInfoByCmd(respData.getDevType(), cmdMark);
        //根据配置解析获取参数值
        FrameParaData paraData = modemInterPrtcService.doGetParam(respData, paramBytes, paraInfoByCmd);
        paraData.setParaOrigByte(paramBytes);
        frameParaDataList.add(paraData);
        respData.setFrameParaList(frameParaDataList);
        //响应结果向下流转
        dataReciveService.paraQueryRecive(respData);
        return respData;
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
        //根据配置将参数值编码为字节
        if (!StringUtils.isBlank(paraVal)) {
            dataBytes = modemScmmPrtcService.doGetFrameBytes(paraData);
        }
        FrameParaInfo info = BaseInfoContainer.getParaInfoByCmd(paraData.getDevType(), reqInfo.getCmdMark());
        String accessRight = info.getNdpaAccessRight();
        String ctrlCmk = info.getNdpaRemark1Data();
        /**该设备读写类型的参数  从remark数据中获取其控制关键字*/
        if (READ_WRITE.equals(accessRight)&&!StringUtils.isBlank(ctrlCmk)){
            reqInfo.setCmdMark(ctrlCmk);
        }
        //获取响应参数cmk对应的控制cmk
        reqInfo.setParamBytes(dataBytes);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        return null;
    }
}
