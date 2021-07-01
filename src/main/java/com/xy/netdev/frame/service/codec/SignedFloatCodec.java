package com.xy.netdev.frame.service.codec;

import com.xy.netdev.frame.service.ParamCodec;
import com.xy.netdev.transit.schedule.FloatAndByte;
import org.springframework.stereotype.Component;

/**
 * 4字节转带符号 浮点数
 */
@Component
public class SignedFloatCodec implements ParamCodec {

    @Override
    public String decode(byte[] bytes, Object... objects) {
        float v = FloatAndByte.bytesToFloat(bytes);
        return String.format("%.2f",v);
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        byte[] bytes = FloatAndByte.floatToBytes(Float.parseFloat(value));
        return bytes;
    }
}
