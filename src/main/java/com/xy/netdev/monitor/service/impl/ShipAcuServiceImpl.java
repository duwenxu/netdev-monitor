package com.xy.netdev.monitor.service.impl;

import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevCtrlInterInfoContainer;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.monitor.bo.Angel;
import com.xy.netdev.monitor.bo.InterfaceViewInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.service.IShipAcuService;
import com.xy.netdev.transit.IDevCmdSendService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 船载1.5米ACU 服务实现类
 *
 * @author admin
 * @date 2021-03-05
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ShipAcuServiceImpl implements IShipAcuService {

    @Autowired
    private IDevCmdSendService devCmdSendService;
    /*星下点自动执行流程码*/
    private static String autoOperStr = "0110,0101";

    /**
     * 手动执行
     * @param angel
     */
    @Override
    public void operCtrl(Angel angel) {
        Interface interface1 = BaseInfoContainer.getPageItfInfo(angel.getDevNo()).get(0);
        InterfaceViewInfo interfaceViewInfo = DevCtrlInterInfoContainer.genInter(angel.getDevNo(),interface1);
        String devStatus = angel.getFunc(); //工作状态
        String az = angel.getAz();; // 方位角
        String el = angel.getEl(); // 俯仰角
        String jc = angel.getJc(); // 交叉角
        String pol = angel.getPol(); // 极化角
        //默认待机、自跟踪、扫描跟踪、手速、指向
        if("0011".equals(angel.getFunc()) || "0100".equals(angel.getFunc())){
            //空间指向
            jc = pol;
            pol = angel.getFreq();
        }else if("1111".equals(angel.getFunc())){
            az = DevParaInfoContainer.getDevParaView(angel.getDevNo(),"2").getParaVal();
            el = DevParaInfoContainer.getDevParaView(angel.getDevNo(),"4").getParaVal();
            jc = DevParaInfoContainer.getDevParaView(angel.getDevNo(),"6").getParaVal();
            pol = DevParaInfoContainer.getDevParaView(angel.getDevNo(),"8").getParaVal();
            if("1".equals(angel.getStepType())){
                az = String.valueOf(Double.parseDouble(az) + Double.parseDouble(angel.getAz()));
            }else if("2".equals(angel.getStepType())){
                el = String.valueOf(Double.parseDouble(el) + Double.parseDouble(angel.getEl()));
            }else if("3".equals(angel.getStepType())){
                jc = String.valueOf(Double.parseDouble(jc) + Double.parseDouble(angel.getJc()));
            }else{
                pol = String.valueOf(Double.parseDouble(pol) + Double.parseDouble(angel.getPol()));
            }
        }
        interfaceViewInfo.getSubParaList().get(0).setParaVal(devStatus);
        interfaceViewInfo.getSubParaList().get(1).setParaVal(az);
        interfaceViewInfo.getSubParaList().get(2).setParaVal(el);
        interfaceViewInfo.getSubParaList().get(3).setParaVal(jc);
        interfaceViewInfo.getSubParaList().get(4).setParaVal(pol);
        devCmdSendService.interfaceCtrSend(interfaceViewInfo);
    }

    /**
     * 自动执行
     * @param angel
     */
    @Override
    public void autoCtrl(Angel angel) {
        autoOperStr = StringUtils.leftPad(autoOperStr,1,angel.getFunc()+",");
        //按顺序执行自动流程
        for (String s : autoOperStr.split(",")) {
            angel.setFunc(s);
            operCtrl(angel);
            try {
                Thread.sleep(2*1000);
            } catch (InterruptedException e) {
                log.debug("休眠发生异常！");
            }
        }
    }

    /**
     * 获取当前位置的经纬度
     * @param angel
     * @return
     */
    @Override
    public Angel getLocalDeg(Angel angel) {
        String devJd = DevParaInfoContainer.getDevParaView(angel.getDevNo(),"30").getParaVal();
        String devWd = DevParaInfoContainer.getDevParaView(angel.getDevNo(),"31").getParaVal();
        angel.setDevJd(devJd);
        angel.setDevWd(devWd);
        return angel;
    }
}
