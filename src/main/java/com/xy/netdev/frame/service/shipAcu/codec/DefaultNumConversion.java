package com.xy.netdev.frame.service.shipAcu.codec;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjectUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.service.ParamCodec;
import org.springframework.stereotype.Component;

/**
 * 缺省数值转换类
 * sunchao
 */
@Component
public class DefaultNumConversion implements ParamCodec {
    @Override
    public String decode(byte[] bytes, Object... objects) {
        String value = String.valueOf(ByteUtils.byteToInt(bytes));
        return value;
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        return new byte[0];
    }
}
