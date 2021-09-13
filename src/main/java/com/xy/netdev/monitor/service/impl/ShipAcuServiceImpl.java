package com.xy.netdev.monitor.service.impl;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevCtrlInterInfoContainer;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.frame.service.shipAcu.thread.AutoAdjustThread;
import com.xy.netdev.monitor.bo.Angel;
import com.xy.netdev.monitor.bo.InterfaceViewInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.service.IShipAcuService;
import com.xy.netdev.transit.IDevCmdSendService;
import lombok.extern.slf4j.Slf4j;
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

    @Autowired
    private ISysParamService sysParamService;

    /*星下点自动执行流程码*/
    private static String autoOperStr = "0110,0101";
    /*步进流程值*/
    private static String stepOperStr = "0.15,0.1,0.05";


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
        if("0011".equals(angel.getFunc())){
            //空间指向
            jc = pol;
            pol = angel.getFreq();
        }else if("0100".equals(angel.getFunc())){
            //星下点
            az = angel.getSatJd();
            el = angel.getSatWd();
            jc = angel.getIsLevel() == true ? "0" : "90";
            pol = angel.getFreq();
        }else if("1111".equals(angel.getFunc())){
            devStatus = "0010";
            //步进
            az = String.format("%.2f",Double.parseDouble(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"2").getParaVal()) + Double.parseDouble(angel.getAz()));
            el = String.format("%.2f",Double.parseDouble(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"4").getParaVal()) + Double.parseDouble(angel.getEl()));
            jc = String.format("%.2f",Double.parseDouble(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"6").getParaVal()) + Double.parseDouble(angel.getJc()));
            pol = String.format("%.2f",Double.parseDouble(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"8").getParaVal()) + Double.parseDouble(angel.getPol()));
        }
        interfaceViewInfo.getSubParaList().get(0).setParaVal(devStatus);
        interfaceViewInfo.getSubParaList().get(1).setParaVal(az);
        interfaceViewInfo.getSubParaList().get(2).setParaVal(el);
        interfaceViewInfo.getSubParaList().get(3).setParaVal(jc);
        interfaceViewInfo.getSubParaList().get(4).setParaVal(pol);
        devCmdSendService.interfaceCtrSend(interfaceViewInfo);
    }

    /**
     * 获取当前位置的经纬度
     * @param angel
     * @return
     */
    @Override
    public Angel getLocalDeg(Angel angel) {
        String devJd = DevParaInfoContainer.getDevParaView(angel.getDevNo(),"23").getParaVal();
        String devWd = DevParaInfoContainer.getDevParaView(angel.getDevNo(),"24").getParaVal();
        angel.setDevJd(devJd);
        angel.setDevWd(devWd);
        return angel;
    }

    /**
     * 获取当前状态
     * @param angel
     * @return
     */
    @Override
    public Angel getCurrentStage(Angel angel) {
        String paraVal = DevParaInfoContainer.getDevParaView(angel.getDevNo(),"1").getParaVal();
        angel.setFunc(paraVal);
        return angel;
    }


    /**
     * 自动执行
     * @param angel
     */
    @Override
    public void autoCtrl(Angel angel) {
        //启动自动化执行线程
        Thread thread = new Thread(new AutoAdjustThread(angel,sysParamService,this));
        thread.setName("船载acu线程");
        thread.start();
    }
}
