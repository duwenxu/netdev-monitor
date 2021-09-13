package com.xy.netdev.frame.service.shipAcu.thread;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.monitor.bo.Angel;
import com.xy.netdev.monitor.service.IShipAcuService;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Collectors;
import static com.xy.netdev.common.constant.SysConfigConstant.*;

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
     */
    private boolean isStop = false;

    /**
     * 自跟踪流程：空间执行，扫描跟踪、自跟踪
     */
    private String steps = "0110,0101";

    public AutoAdjustThread(Angel angel,ISysParamService sysParamService,IShipAcuService shipAcuService){
        this.angel = angel;
        this.sysParamService = sysParamService;
        this.shipAcuService = shipAcuService;
    }

    @Override
    public void run() {
        //将初次的命令标识添加到自跟踪流程中
        steps = angel.getFunc()+"," + steps ;
        flag : while (true) {
            try {
                //按照命令顺序依次执行
                for(String step : steps.split(",")){
                    angel.setFunc(step);
                    //执行命令
                    shipAcuService.operCtrl(angel);
                    log.info("执行命令"+angel.getFunc()+"完成！");
                    if("0011".equals(step)){
                        this.adjustAngle(0.15,-0.1);
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
                }
            } catch (Exception e) {
                log.error("船载acu自动化流程发生异常！");
            }
        }
    }

    /**
     * 等待acu、俯仰、计划到位
     */
    private void WaitAngleGet() throws Exception{
        while(true){
            //判断方位角是否到位
            if(Double.valueOf(angel.getAz()).equals(Double.valueOf(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"2").getParaVal()))){
                //判断俯仰角是否到位
                if(Double.valueOf(angel.getEl()).equals(Double.valueOf(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"4").getParaVal()))){
                    //判断计划角是否到位
                    if(Double.valueOf(angel.getPol()).equals(Double.valueOf(DevParaInfoContainer.getDevParaView(angel.getDevNo(),"8").getParaVal()))){
                        log.info("方位俯仰极化角度已到位");
                        break;
                    }
                }
            }
            Thread.sleep(Long.valueOf(sysParamService.getParaRemark1(ACU_SLEEP_TIME)));
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
        isNext = isNext(angel.getDevNo());
        if (isNext) {
            //当uage>门槛电压值且锁定时继续下一个步进调节
            do {
                //重新设置方位角进行调整
                angel.setAz(String.valueOf(Double.valueOf(angel.getAz()) + nextStepping));
                shipAcuService.operCtrl(angel);
                isNext = isNext(angel.getDevNo());
            } while (!isNext && !isStop);
            log.info("调整方位角完成,当前步进："+currStepping+",下一步进："+nextStepping);
            currStepping = nextStepping;
            nextStepping = 0.05;
        } else {
            do {
                angel.setAz(String.valueOf(Double.valueOf(angel.getAz()) + currStepping));
                shipAcuService.operCtrl(angel);
                isNext = isNext(angel.getDevNo());
            } while (!isNext && !isStop);
            log.info("调整方位角执行,当前步进："+currStepping+",下一步进："+nextStepping);
        }
        if(isStop){
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
    private boolean isNext(String devNo) throws Exception{
        if("1".equals(DevParaInfoContainer.getDevParaView(devNo,"73"))){
            isStop = true;
            return false;
        }
        //判断方位、俯仰、极化是否到位
        this.WaitAngleGet();
        String agcValue = DevParaInfoContainer.getDevParaView(devNo,"9").getParaVal(); //agc
        String recvStatus = DevParaInfoContainer.getDevParaView(devNo,"65").getSubParaList().stream().filter(paraViewInfo -> paraViewInfo.getParaNo().equals("66")).collect(Collectors.toList()).get(0).getParaVal();  //接收机状态
        if(Double.parseDouble(agcValue)> Double.parseDouble(sysParamService.getParaRemark1(ACU_AGE_VALUE)) && recvStatus.equals("0")){
            return true;
        }
        return false;
    }
}
