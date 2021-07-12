package com.xy.netdev.frame.service.tkuka.codec;

import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.service.ParamCodec;
import org.springframework.stereotype.Component;

/**
 * 版本解析处理
 *
 * @author sunchao
 * @create 2021-07-05 17:08
 */
@Component
public class VersionParamCodeC implements ParamCodec {
    @Override
    public String decode(byte[] bytes, Object... objects) {
        float number = ByteUtils.byteToNumber(bytes,0,bytes.length).floatValue()/1000;
        return "V"+number;
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        return new byte[0];
    }
}
