package com.xy.netdev.transit.impl;

import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.DevLogInfoContainer;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.snmp.SnmpResDTO;
import com.xy.netdev.transit.ISnmpDataReceiveService;
import com.xy.netdev.websocket.send.DevIfeMegSend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * SNMP接收数据处理实现类
 */
@Component
@Slf4j
public class SnmpDataReceiveServiceImpl implements ISnmpDataReceiveService {

    @Autowired
    private DataReciveServiceImpl dataReciveService;

    @Override
    public void paraQueryRecive(SnmpResDTO snmpResDTO) {
        snmpResDTO.setOperType(SysConfigConstant.OPREATE_QUERY_RESP);
        FrameRespData respData = convertFrameDto(snmpResDTO);
        assert respData != null;
        if(DevParaInfoContainer.handlerRespDevPara(respData)){
            DevIfeMegSend.sendParaToDev(respData.getDevNo());//如果设备参数变化,websocet推前台
            dataReciveService.sendCtrlInter(respData);
            //站控主动上报
            dataReciveService.rptDevExecutor.submit(()->dataReciveService.stationRptParamsByDev(respData));
        }
        DevLogInfoContainer.handlerRespDevPara(respData);//记录日志
        DevIfeMegSend.sendLogToDev(respData.getDevNo());//操作日志websocet推前台
//        handlerAlertInfo(respData);//处理报警、主备等信息
    }

    private FrameRespData convertFrameDto(SnmpResDTO snmpResDTO) {
        List<FrameParaData> paraDataList = new ArrayList<>(1);
        FrameParaData frameParaData = FrameParaData.builder()
                .devType(snmpResDTO.getDevType())
                .paraNo(snmpResDTO.getParaNo())
                .devNo(snmpResDTO.getDevNo())
                .paraVal(snmpResDTO.getParaVal())
                .len(snmpResDTO.getLen())
                .build();
        paraDataList.add(frameParaData);
        FrameRespData respData = FrameRespData.builder()
                .accessType(snmpResDTO.getAccessType())
                .cmdMark(snmpResDTO.getCmdMark())
                .devNo(snmpResDTO.getDevNo())
                .frameParaList(paraDataList)
                .devType(snmpResDTO.getDevType())
                .operType(snmpResDTO.getOperType())
                .build();
        return respData;
    }

}
