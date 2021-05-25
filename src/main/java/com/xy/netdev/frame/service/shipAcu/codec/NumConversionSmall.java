package com.xy.netdev.frame.service.shipAcu.codec;

import cn.hutool.core.util.ObjectUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.service.ParamCodec;
import org.springframework.stereotype.Component;

/**
 * 小端数值转换类
 * sunchao
 */
@Component
public class NumConversionSmall implements ParamCodec {
    @Override
    public String decode(byte[] bytes, Object... objects) {
        //默认不做小数点保留
        double value = 1;//利用小端转换
        if (objects != null&&objects.length!=0) {
            if (!ObjectUtil.isEmpty(objects[0])){
                value = ByteUtils.Bytes2Int_LE(bytes)/Double.valueOf(objects[0].toString());
            }
        }
        return String.format("%.2f",value);
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        //默认不做小数点保留
        byte[] bytes = new byte[]{};//利用小端转换
        Double result = 1.0;
        if (objects != null&&objects.length!=0) {
            if (!ObjectUtil.isEmpty(objects[0])){
                result = Double.valueOf(value) * Double.valueOf(objects[0].toString());
            }
        }
        return ByteUtils.objToBytes(result,Integer.valueOf(objects[1].toString()),true);
    }
}
