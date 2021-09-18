package com.xy.netdev.frame.service.shipAcu.thread;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.ParaHandlerUtil;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.monitor.bo.Angel;
import com.xy.netdev.monitor.service.IShipAcuService;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Collectors;
import static com.xy.netdev.common.constant.SysConfigConstant.ACU_AGE_VALUE;
import static com.xy.netdev.common.constant.SysConfigConstant.ACU_SLEEP_TIME;

/**
 * @function 船载acu自动执行方位角调整线程
 * @Author  sun
 * @time   2021-09-09
 */
@Slf4j
public class AutoAdjustThread implements Runnable{


    private static ISysParamService sysParamService;

    private static IShipAcuService shipAcuService;

    /**
     * 地面站和卫星经纬度实体类
     */
    private Angel angel;

    /**
     * 是否继续下一步标识
     */
    private boolean isNext = false;

    /**
     * 是否退出整个线程标识
     *//*
    private boolean isStop = false;*/

   /* *//**
     * 自跟踪流程：空间执行，扫描跟踪、自跟踪
     *//*
    private String steps = "0110,0111";*/

    public AutoAdjustThread(Angel angel,ISysParamService sysParamService,IShipAcuService shipAcuService){
        this.angel = angel;
        this.sysParamService = sysParamService;
        this.shipAcuService = shipAcuService;
    }

    @Override
    public void run() {
        //将初次的命令标识添加到自跟踪流程中
        /*steps = angel.getFunc()+"," + steps ;*/
        flag : while (true && !"1".equals(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"73"))) {
            try {
                //空间指向
                this.execOne();
                //扫描跟踪
                this.execTwo();
                //自跟踪
                this.execThree();
                result:while(true && !"1".equals(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"73"))){
                    if("1".equals(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"73"))){
                        log.info("工作模式标志位参数值发生变化，递归返回！");
                        //当线程停止标志发生时，立马退出
                        break flag;
                    }
                    /*if(isNext(angel.getDevNo(),true)){
                         Thread.sleep(60*60*1000);
                         //扫描跟踪
                         this.execTwo();
                        //自跟踪
                        this.execThree();
                    }else */if(!isNext(angel.getDevNo(),false)){
                        break result;
                    }
                }
                break flag;
            } catch (Exception e) {
                log.error("船载acu自动化流程发生异常！");
            }
            if("1".equals(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"73"))){
                log.info("工作模式标志位参数值发生变化，递归返回！");
                //当线程停止标志发生时，立马退出
                break flag;
            }
                //按照命令顺序依次执行
                /*for(String step : steps.split(",")){
                    angel.setFunc(step);
                    //执行命令
                    shipAcuService.operCtrl(angel);
                    log.info("执行命令"+angel.getFunc()+"完成！");
                    if("0011".equals(step)){
                        //等待角度到位
                        this.waitAngleGet();
                    }
                    if("0111".equals(step)){
                        //当为自跟踪时判断agc和锁定状态
                        if(!isNext(angel.getDevNo())){

                        }
                    }
                    if(isStop){
                        log.info("工作模式设置标识发生变化，即将退出！");
                        //退出最外层循环，线程结束
                        break flag;
                    }
                }
                //自跟踪指令发送完成后，若Uagc小于门槛电压，又返回第一步进行自动扫描流程
                if(Double.parseDouble(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"9").getParaVal())> Double.parseDouble(sysParamService.getParaRemark1(ACU_AGE_VALUE))){
                    //退出最外层循环，线程结束
                    log.info("自动化流程完成！，退出线程！");
                    break flag;
                }*/
        }
        log.error("退出自跟踪线程！");
        DevParaInfoContainer.updateParaValue(angel.getDevNo(), ParaHandlerUtil.genLinkKey(angel.getDevNo(),"73"),"0");
    }

    /**
     * 执行空间指向/星下点
     * @throws Exception
     */
    private void execOne() throws Exception{
        if("1".equals(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"73"))){
            log.info("工作模式标志位参数值发生变化，递归返回！");
            //当线程停止标志发生时，立马退出
            return;
        }
        //空间指向
        shipAcuService.operCtrl(angel);
        //等待角度到位
        waitAngleGet();
        //判断uagc和锁定状态
        /*if(!isNext(angel.getDevNo(),true)){
            if("0011".equals(angel.getFunc())){
                this.adjustAngle(0.15,-1);
            }
        }*/
        log.error("执行空间指向/星下点完成！");
    }

    /**
     * 执行扫描跟踪
     * @throws Exception
     */
    private void execTwo() throws Exception{
        if("1".equals(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"73"))){
            log.info("工作模式标志位参数值发生变化，递归返回！");
            //当线程停止标志发生时，立马退出
            return;
        }
        //发送扫描跟踪
        angel.setFunc("0110");
        shipAcuService.operCtrl(angel);
        Thread.sleep(Long.valueOf(sysParamService.getParaRemark1(ACU_SLEEP_TIME))*1000);
        log.error("执行扫描跟踪");
    }

    /**
     * 执行自跟踪
     * @throws Exception
     */
    private void execThree() throws Exception{
        if("1".equals(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"73"))){
            log.info("工作模式标志位参数值发生变化，递归返回！");
            //当线程停止标志发生时，立马退出
            return;
        }
        //发送自跟踪指令
        angel.setFunc("0111");
        shipAcuService.operCtrl(angel);
        log.error("执行自跟踪完成！");
    }

    /**
     * 等待acu、俯仰、计划到位
     */
    private void waitAngleGet() throws Exception{
        while(true && !"1".equals(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"73"))){
            Thread.sleep(Long.valueOf(sysParamService.getParaRemark1(ACU_SLEEP_TIME))*1000);
            //判断方位角是否到位
            if(Math.abs(Double.valueOf(angel.getAz())) - Math.abs(Double.valueOf(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"58").getSubParaList().stream().filter(paraViewInfo -> paraViewInfo.getParaNo().equals("62")).collect(Collectors.toList()).get(0).getParaVal())) < 0.1){
                //判断俯仰角是否到位
                if(Math.abs(Double.valueOf(angel.getEl())) - Math.abs(Double.valueOf(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"58").getSubParaList().stream().filter(paraViewInfo -> paraViewInfo.getParaNo().equals("63")).collect(Collectors.toList()).get(0).getParaVal())) < 0.1){
                    //判断计划角是否到位
                    double pol = Double.valueOf(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"58").getSubParaList().stream().filter(paraViewInfo -> paraViewInfo.getParaNo().equals("64")).collect(Collectors.toList()).get(0).getParaVal());
                    if (pol < 0) {
                        pol = pol + 180;
                    }else if(pol>180){
                        pol = pol - 180;
                    }
                    if(Math.abs(Double.valueOf(angel.getPol())) - pol <1){
                        log.info("方位俯仰极化角度已到位");
                        break;
                    }
                }
            }
            /*if("1".equals(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"73"))){
                isStop = true;
                break;
            }*/
        }
    }

    /**
     * 方位角调整（递归）
     * @param currStepping 当前步进值
     * @param nextStepping 下一步进值
     */
    private void adjustAngle(double currStepping, double nextStepping) throws Exception {
        log.info("调整方位角执行,当前步进："+currStepping+",下一步进："+nextStepping);
        //解决此方法第一次进来赋值问题
        isNext = isNext(angel.getDevNo(),true);
        if (isNext) {
            //当uage>门槛电压值且锁定时继续下一个步进调节
            do {
                //重新设置方位角进行调整
                angel.setAz(String.valueOf(Double.valueOf(angel.getAz()) + nextStepping));
                shipAcuService.operCtrl(angel);
                isNext = isNext(angel.getDevNo(),true);
            } while (!isNext && !"1".equals(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"73")));
            log.info("调整方位角完成,当前步进："+currStepping+",下一步进："+nextStepping);
            currStepping = nextStepping;
            nextStepping = 0.05;
        } else {
            do {
                angel.setAz(String.valueOf(Double.valueOf(angel.getAz()) + currStepping));
                shipAcuService.operCtrl(angel);
                isNext = isNext(angel.getDevNo(),true);
            } while (!isNext && !"1".equals(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"73")));
            log.info("调整方位角执行,当前步进："+currStepping+",下一步进："+nextStepping);
        }
        if("1".equals(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"73"))){
            log.info("工作模式标志位参数值发生变化，递归返回！");
            //当线程停止标志发生时，立马退出
            return;
        }
        if (currStepping != 0.05) {
            //当前步进不等于0.05时才继续递归
            adjustAngle(currStepping, nextStepping);
        }
    }

    /**
     * 通过门槛电压值接收机锁定状态判断是否继续下一步
     * @param devNo
     * @return
     */
    private boolean isNext(String devNo,boolean isAgc) throws Exception {
        /*if("1".equals(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"73"))){
            isStop = true;
            return false;
        }*/
        Thread.sleep(Long.valueOf(sysParamService.getParaRemark1(ACU_SLEEP_TIME))*1000);
        String agcValue = DevParaInfoContainer.getDevParaView(devNo,"9").getParaVal(); //agc
        String recvStatus = DevParaInfoContainer.getDevParaView(devNo,"65").getSubParaList().stream().filter(paraViewInfo -> paraViewInfo.getParaNo().equals("66")).collect(Collectors.toList()).get(0).getParaVal();  //接收机状态
        if(isAgc){
            if(Double.parseDouble(agcValue)> Double.parseDouble(sysParamService.getParaRemark1(ACU_AGE_VALUE)) && recvStatus.equals("1")){
                return true;
            }
            return false;
        }else{
            if(Double.parseDouble(agcValue)> Double.parseDouble(sysParamService.getParaRemark1(ACU_AGE_VALUE))){
                return true;
            }
            return false;
        }
    }
}
