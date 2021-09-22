package com.xy.netdev.frame.service.codec;

import cn.hutool.core.codec.BCD;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjectUtil;
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
        long parseLong = Long.parseLong(s);
        return parseLong+"";

//        double divideValue = 1;
//        if (objects != null && objects.length != 0) {
//            divideValue = Math.pow(10, (int) objects[0]);
//        }
//        StringBuilder value = new StringBuilder(new BigDecimal(Long.parseLong(s) / divideValue + "").toString());
    }

    /**
     * 参数编码 参数值转字节数组
     *
     * @param value   编码值
     * @param objects 可能需要的其他传入参数  参数1：保留的小数点位数  参数2：需要转换为byte[]字节数的String长度
     * @return
     */
    @Override
    public byte[] encode(String value, Object... objects) {
        //默认不做小数点保留
        double rideValue = 1;
        if (objects != null && objects.length != 0) {
            rideValue = Math.pow(10, (int) objects[0]);
        }
        int strLen = 8;
        if(objects!=null&&objects.length==2){
            if (!ObjectUtil.isEmpty(objects[1])){
                strLen = (Integer)objects[1];
            }
        }
        StringBuilder s = new StringBuilder(new BigDecimal(Double.parseDouble(value) * rideValue).toString());
        //位数不够，在字节前补0
        while (s.length()<strLen){
            s.insert(0, "0");
        }
        return HexUtil.decodeHex(s.toString());
    }

    public static void main(String[] args) {
//        BcdParamCodec bcdParamCodec = new BcdParamCodec();
//        byte[] bytes = {0x00, (byte) 0xFA};
//        String decode = bcdParamCodec.decode(bytes);
//        System.out.println(decode);
//        byte[] encode = bcdParamCodec.encode(decode);
//        System.out.println(HexUtil.encodeHexStr(encode).toUpperCase());



    }
}