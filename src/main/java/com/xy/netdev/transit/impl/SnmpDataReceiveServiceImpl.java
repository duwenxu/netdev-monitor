package com.xy.netdev.transit.impl;

import com.xy.netdev.container.DevLogInfoContainer;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.snmp.SnmpResDTO;
import com.xy.netdev.transit.ISnmpDataReceiveService;
import com.xy.netdev.websocket.send.DevIfeMegSend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * SNMP接收数据处理实现类.
 */
@Component
@Slf4j
public class SnmpDataReceiveServiceImpl implements ISnmpDataReceiveService {

    @Autowired
    private DataReciveServiceImpl dataReciveService;

    @Override
    public void paraQueryRecive(SnmpResDTO snmpResDTO) {
        FrameRespData respData = convertFrameResDto(snmpResDTO);
        assert respData != null;

        if (DevParaInfoContainer.handlerRespDevPara(respData)) {
            DevIfeMegSend.sendParaToDev(respData.getDevNo());//如果设备参数变化,websocet推前台
            //TODO 更新SNMP数据
            dataReciveService.sendCtrlInter(respData);
            //站控主动上报
//            dataReciveService.rptDevExecutor.submit(() -> dataReciveService.stationRptParamsByDev(respData));
        }
        DevLogInfoContainer.handlerRespDevPara(respData);//记录日志
        DevIfeMegSend.sendLogToDev(respData.getDevNo());//操作日志websocet推前台
//        handlerAlertInfo(respData);//处理报警、主备等信息
    }

    public FrameRespData convertFrameResDto(SnmpResDTO snmpResDTO) {
        FrameRespData respData = FrameRespData.builder()
                .accessType(snmpResDTO.getAccessType())
                .cmdMark(snmpResDTO.getCmdMark())
                .devNo(snmpResDTO.getDevNo())
                .frameParaList(snmpResDTO.getFrameParaList())
                .devType(snmpResDTO.getDevType())
                .operType(snmpResDTO.getOperType())
                .build();
        return respData;
    }

}
