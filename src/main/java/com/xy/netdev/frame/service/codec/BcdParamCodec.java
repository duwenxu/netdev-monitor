package com.xy.netdev.frame.service.codec;

import cn.hutool.core.codec.BCD;
import com.xy.netdev.frame.service.ParamCodec;

/**
 * BCD码编解码实现
 *
 * @author duwenxu
 * @create 2021-04-02 17:06
 */
public class BcdParamCodec implements ParamCodec {

    /**
     * 返回指定小数点位分割的BCD码标识的String
     * @param bytes 原始字节
     * @param point 小数点位置
     * @return 格式化String
     */
    @Override
    public String decode(byte[] bytes, Object... objects) {
        int point = (int) objects[0];
        String s = BCD.bcdToStr(bytes);
        StringBuilder sb = new StringBuilder();
        if (s.length()>point){
            String beforePoint = s.substring(0, s.length() - point);
            String afterPoint = s.substring(s.length() - point);
            while (beforePoint.startsWith("0") && beforePoint.length()>1){
                beforePoint = beforePoint.substring(1);
            }
            while (afterPoint.startsWith("0") && afterPoint.length()>1){
                afterPoint = afterPoint.substring(1);
            }
            sb = new StringBuilder(beforePoint).append(".").append(afterPoint);
        }
        return sb.toString();
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        return new byte[0];
    }
}
