package com.xy.netdev.frame.service.codec.freqcov;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjectUtil;
import com.xy.netdev.frame.service.ParamCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 6914变频器--衰减值 编解码器
 *
 * @author duwenxu
 * @create 2021-05-18 13:40
 */
@Component
@Slf4j
public class AttenuationCodec implements ParamCodec {

    @Override
    public String decode(byte[] bytes, Object... objects) {
        //默认不做小数点保留
        double divideValue = 1;
        if (objects != null&&objects.length!=0) {
            if (!ObjectUtil.isEmpty(objects[0])){
                divideValue = Math.pow(10, (int) objects[0]);
            }
        }
        /**衰减值以 0.25为单位  解析时的值要 /4*/
        divideValue = divideValue * 4;
        long originValue = Long.parseLong(HexUtil.encodeHexStr(bytes), 16);
        return new BigDecimal(originValue / divideValue + "").toString();
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        //默认不做小数点保留
        double rideValue = 1;
        if (objects!=null&&objects.length!=0){
            if (!ObjectUtil.isEmpty(objects[0])){
                rideValue = Math.pow(10, (int) objects[0]);
            }
        }
        String format ="%08X";
        String fmtStart = "%0";
        String fmtEnd = "X";
        if (objects!=null&&objects.length>1){
            format = fmtStart + (int) objects[1] * 2 + "" + fmtEnd;
        }
        /**衰减值以 0.25为单位 控制时值要 * 4*/
        rideValue = rideValue*4;
        String hexStr = String.format(format, (long)(Double.parseDouble(value)*rideValue));
        return HexUtil.decodeHex(hexStr);
    }
}
