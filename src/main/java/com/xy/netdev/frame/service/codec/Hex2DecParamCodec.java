package com.xy.netdev.frame.service.codec;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjectUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.service.ParamCodec;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 16进制转10进制并 保留指定的小数位数
 *
 * @author duwenxu
 * @create 2021-04-08 11:11
 */
@Component
public class Hex2DecParamCodec implements ParamCodec {

    @Override
    public String decode(byte[] bytes, Object... objects) {
        //默认不做小数点保留
        double divideValue = 1;
        if (objects != null&&objects.length!=0) {
            if (!ObjectUtil.isEmpty(objects[0])){
                divideValue = Math.pow(10, (int) objects[0]);
            }
        }
        long originValue = Long.parseLong(HexUtil.encodeHexStr(bytes), 16);
        return new BigDecimal(originValue / divideValue + "").toString();
    }

    /**
     *
     * @param value   编码值
     * @param objects 可能需要的其他传入参数  参数一：保留几位小数   参数二：字节长度，转换为几位16进制(默认为4转换为8位16进制)
     * @return
     */
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
        String hexStr = String.format(format, (long)(Double.parseDouble(value)*rideValue));
        return HexUtil.decodeHex(hexStr);
    }

    public static void main(String[] args) {
//        byte[] bytes = {0x00, 0x13, 0x59, 0x20};
        byte[] bytes = {0x1e};
        Hex2DecParamCodec hex2DecParamCodec = new Hex2DecParamCodec();
        String decode = hex2DecParamCodec.decode(bytes);
        System.out.println(decode);
        byte[] encode = hex2DecParamCodec.encode(decode);
        System.out.println(HexUtil.encodeHex(encode));
    }
}
