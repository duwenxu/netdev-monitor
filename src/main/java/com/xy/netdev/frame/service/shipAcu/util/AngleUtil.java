package com.xy.netdev.frame.service.shipAcu.util;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 角度计算辅助类
 * @author sunchao
 * @create 2021-05-19 11:08
 */
@Component
public class AngleUtil {

    /**
     * 计算角度
     * @param satJd 卫星经度
     * @param devJd  本地经度
     * @param devWd  本地纬度
     * @param isLevel  是否水平
     * @return
     */
    public static Map<String,String> ctrlAngel(double satJd,double devJd,double devWd,boolean isLevel){
        Map<String,String> map = new HashMap();
        map.put("az",String.valueOf(getAZ(satJd,devJd,devWd,isLevel)));
        map.put("el",String.valueOf(getEL(satJd,devJd,devWd,isLevel)));
        map.put("pol",String.valueOf(getPOL(satJd,devJd,devWd,isLevel)));
        return map;
    }

    /**
     * 利用java计算方位角
     * @return
     */
    public static double getAZ(double satJd,double devJd,double devWd, boolean isLevel){
        satJd = satJd * Math.PI/180;
        devJd = devJd * Math.PI/180;
        devWd = devWd * Math.PI/180;
        double az = 180 - 180/Math.PI*Math.atan(Math.tan(satJd-devJd)/Math.sin(devWd));
        return Double.parseDouble(String.format("%.2f",az));
    }

    /**
     * 利用java计算俯仰角
     *
     * @return
     */
    public static double getEL(double satJd,double devJd,double devWd, boolean isLevel){
        satJd = satJd * Math.PI/180;
        devJd = devJd * Math.PI/180;
        devWd = devWd * Math.PI/180;
        double dLong = satJd - devJd;
        double az = 180/Math.PI*Math.atan((Math.cos(dLong)*Math.cos(devWd)-0.15127)/Math.sqrt(1-Math.pow(Math.cos(dLong)*Math.cos(devWd),2)));
        return Double.parseDouble(String.format("%.2f",az));
    }

    /**
     * 利用java计算极化角
     * @return
     */
    public static double getPOL(double satJd,double devJd,double devWd, boolean isLevel){
        satJd = satJd * Math.PI / 180;
        devJd = devJd * Math.PI / 180;
        devWd = devWd * Math.PI / 180;
        double dLong = satJd - devJd;
        double pol = 180 / Math.PI * Math.atan(Math.sin(-dLong) / Math.tan(devWd));
        if (!isLevel) {
            pol = pol + 90;
            if (pol > 225) {
                pol = pol - 90;
            }
        }
        if (pol < 0) {
            pol = pol + 180;
        }
        return Double.parseDouble(String.format("%.2f",pol));
    }
}
