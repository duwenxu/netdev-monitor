package com.xy.netdev.frame.service.shipAcu.codec;

import cn.hutool.core.util.ObjectUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.service.ParamCodec;
import org.springframework.stereotype.Component;

/**
 * 无端数值转换类
 * sunchao
 */
@Component
public class NumConversionNo implements ParamCodec {
    @Override
    public String decode(byte[] bytes, Object... objects) {
        //默认不做小数点保留
        double value = 1;
        if (objects != null&&objects.length!=0) {
            if (!ObjectUtil.isEmpty(objects[0])){
                value = ByteUtils.byteToInt(bytes)/Double.valueOf(objects[0].toString());
            }
        }
        return String.valueOf(value);
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        return new byte[0];
    }
}
