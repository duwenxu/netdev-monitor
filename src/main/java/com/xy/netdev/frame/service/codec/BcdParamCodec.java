package com.xy.netdev.frame.service.codec;

import cn.hutool.core.codec.BCD;
import cn.hutool.core.util.HexUtil;
import com.xy.netdev.frame.service.ParamCodec;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * BCD码编解码实现
 *
 * @author duwenxu
 * @create 2021-04-02 17:06
 */
@Component
public class BcdParamCodec implements ParamCodec {

    /**
     * 返回指定小数点位分割的BCD码标识的String
     *
     * @param bytes 原始字节
     * @return 格式化String
     */
    @Override
    public String decode(byte[] bytes, Object... objects) {
        String s = BCD.bcdToStr(bytes);
        double divideValue = 1;
        if (objects != null && objects.length != 0) {
            divideValue = Math.pow(10, (int) objects[0]);
        }
        StringBuilder value = new StringBuilder(new BigDecimal(Long.parseLong(s) / divideValue + "").toString());
        while (value.length()<8){
            value.append("0");
        }
        return value.toString();
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        //默认不做小数点保留
        double rideValue = 1;
        if (objects != null && objects.length != 0) {
            rideValue = Math.pow(10, (int) objects[0]);
        }
        String s = new BigDecimal(Double.parseDouble(value) * rideValue).toString();
        return HexUtil.decodeHex(s);
    }

    public static void main(String[] args) {
        BcdParamCodec bcdParamCodec = new BcdParamCodec();
        byte[] bytes = {0x14, 0x50, 0x00, 0x00};
        String decode = bcdParamCodec.decode(bytes,5);
        System.out.println(decode);
        byte[] encode = bcdParamCodec.encode(decode,5);
        System.out.println(HexUtil.encodeHexStr(encode).toUpperCase());
    }
}
