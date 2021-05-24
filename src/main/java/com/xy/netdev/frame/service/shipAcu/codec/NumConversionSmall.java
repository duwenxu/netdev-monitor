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
        return String.valueOf(value);
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        return new byte[0];
    }
}
