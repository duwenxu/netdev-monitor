package com.xy.netdev.monitor.service.impl;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevCtrlInterInfoContainer;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.monitor.bo.Angel;
import com.xy.netdev.monitor.bo.InterfaceViewInfo;
import com.xy.netdev.monitor.entity.Interface;
import com.xy.netdev.monitor.service.IShipAcuService;
import com.xy.netdev.transit.IDevCmdSendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;

import java.util.stream.Collectors;

import static com.xy.netdev.common.constant.SysConfigConstant.*;

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
     * 自动执行
     * @param angel
     */
    @Override
    public void autoCtrl(Angel angel) {
        //autoOperStr = StringUtils.leftPad(autoOperStr,1,angel.getFunc()+",");
        operCtrl(angel);
        try {
            isStage(angel);
            //按顺序执行自动流程
            for (String s : autoOperStr.split(",")) {
                angel.setFunc(s);
                operCtrl(angel);
                try {
                    Thread.sleep(Long.valueOf(sysParamService.getParaRemark1(ACU_SLEEP_TIME)));
                } catch (Exception e) {

                }
            }
        } catch (InterruptedException e) {
            log.debug("1.5米acu发生异常！");
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

    /**
     * 获取当前状态
     * @param angel
     * @return
     */
    @Override
    public Angel getCurrentStage(Angel angel) {
        String paraVal = DevParaInfoContainer.getDevParaView(angel.getDevNo(),"1").getSubParaList().stream().filter(paraViewInfo -> paraViewInfo.getParaNo().equals("20")).collect(Collectors.toList()).get(0).getParaVal();
        angel.setFunc(paraVal);
        return angel;
    }


    /**
     * 自动化
     * @param angel
     * @throws InterruptedException
     */
    private void isStage(Angel angel) throws InterruptedException {
        String value = "0";
        if(isNext(angel.getDevNo())){
            value = "-1";
            exec(angel,value);
            if(isNext(angel.getDevNo())){
                value = "0.05";
                exec(angel,value);
            }
        }else{
            value = "0.15";
            exec(angel,value);
            if(isNext(angel.getDevNo())){
                value = "-1";
                exec(angel,value);
                if(isNext(angel.getDevNo())){
                    value = "0.05";
                    exec(angel,value);
                }
            }
        }
    }

    /**
     * 方位角微调
     * @param angel
     * @param value
     * @throws InterruptedException
     */
    private void exec(Angel angel,String value) throws InterruptedException {
        while(isNext(angel.getDevNo())){
            angel.setFunc("0011");
            angel.setAz(value);
            operCtrl(angel);
            Thread.sleep(Long.valueOf(sysParamService.getParaRemark1(ACU_SLEEP_TIME)));
        }
        angel.setFunc("0011");
        angel.setAz((-Double.valueOf(value)) + "");
        operCtrl(angel);
        Thread.sleep(Long.valueOf(sysParamService.getParaRemark1(ACU_SLEEP_TIME)));
    }

    /**
     * 判断
     * @param devNo
     * @return
     */
    private boolean isNext(String devNo){
        String agc = DevParaInfoContainer.getDevParaView(devNo,"9").getParaVal(); //agc
        String recvStatus = DevParaInfoContainer.getDevParaView(devNo,"48").getSubParaList().stream().filter(paraViewInfo -> paraViewInfo.getParaNo().equals("76")).collect(Collectors.toList()).get(0).getParaVal();  //接收机状态
        if(Double.parseDouble(agc)> Double.parseDouble(sysParamService.getParaRemark1(ACU_AGE_VALUE)) && recvStatus.equals("1")){
            return true;
        }
        return false;
    }
}
