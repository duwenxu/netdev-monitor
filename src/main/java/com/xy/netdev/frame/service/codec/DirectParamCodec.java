package com.xy.netdev.frame.service.codec;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.frame.service.ParamCodec;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 16进制byte[]直接转换
 *
 * @author duwenxu
 * @create 2021-04-02 17:08
 */
@Component
public class DirectParamCodec implements ParamCodec {

    @Override
    public String decode(byte[] bytes, Object... objects) {
        return HexUtil.encodeHexStr(bytes).toUpperCase();
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        return HexUtil.decodeHex(value);
    }
}
