package com.xy.netdev.frame.service.codec;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.frame.service.ParamCodec;
import org.springframework.stereotype.Component;

/**
 * 16进制转10进制
 *
 * @author duwenxu
 * @create 2021-04-08 11:11
 */
@Component
public class Hex2DecParamCodec implements ParamCodec {

    @Override
    public String decode(byte[] bytes, Object... objects) {
        return Long.parseLong(HexUtil.encodeHexStr(bytes),16)+"";
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        String hexStr = String.format("%08X", Long.parseLong(value));
        return HexUtil.decodeHex(hexStr);
    }
}
