package com.xy.netdev.frame.service.codec;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.frame.service.ParamCodec;
import org.springframework.stereotype.Component;

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
        if (objects != null && objects.length != 0) {
            divideValue = Math.pow(10, (int) objects[0]);
        }
        long originValue = Long.parseLong(HexUtil.encodeHexStr(bytes), 16);
        if (divideValue != 1) {
            return originValue / divideValue + "";
        } else {
            return originValue + "";
        }
    }

    /**
     *
     * @param value   编码值
     * @param objects obj[0] 保留小数位数  obj[1]转换后字节数组的长度
     * @return
     */
    @Override
    public byte[] encode(String value, Object... objects) {
        //默认不做小数点保留
        double rideValue = 1;
        if (objects != null && objects.length != 0) {
            rideValue = Math.pow(10, (int) objects[0]);
        }
        String format = "%08X";
        if (objects != null && objects.length > 1 && objects[1] != null) {
            format = "%0" + objects[1] + "X";
        }
        String hexStr = String.format(format, (long) (Double.parseDouble(value) * rideValue));
        return HexUtil.decodeHex(hexStr);
    }

    public static void main(String[] args) {
        byte[] bytes = {0x13, (byte) 0x88};
//        byte[] bytes = {(byte)0xC0, (byte) 0xA8,(byte)0x0A,(byte)0x16};
        Hex2DecParamCodec hex2DecParamCodec = new Hex2DecParamCodec();
        String decode = hex2DecParamCodec.decode(bytes,0);
        System.out.println(decode);
        byte[] encode = hex2DecParamCodec.encode(decode,0,4);
        System.out.println(HexUtil.encodeHexStr(encode));
    }
}
