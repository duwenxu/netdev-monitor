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
        if (objects != null) {
            divideValue = Math.pow(10, (int) objects[0]);
        }
        long originValue = Long.parseLong(HexUtil.encodeHexStr(bytes), 16);
        return originValue / divideValue + "";
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        //默认不做小数点保留
        double rideValue = 1;
        if (objects!=null){
            rideValue = Math.pow(10, (int) objects[0]);
        }
        String hexStr = String.format("%08X", (long)(Double.parseDouble(value)*rideValue));
        return HexUtil.decodeHex(hexStr);
    }

    public static void main(String[] args) {
        byte[] bytes = {0x01, 0x43, 0x15, (byte) 0x8D};
        Hex2DecParamCodec hex2DecParamCodec = new Hex2DecParamCodec();
        String decode = hex2DecParamCodec.decode(bytes,3);
        System.out.println(decode);
        byte[] encode = hex2DecParamCodec.encode(decode, 3);
        System.out.println(encode);
    }
}
