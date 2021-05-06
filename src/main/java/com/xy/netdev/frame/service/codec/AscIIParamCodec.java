package com.xy.netdev.frame.service.codec;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.Charsets;
import com.xy.netdev.frame.service.ParamCodec;
import org.springframework.stereotype.Component;

/**
 * ASCII码编解码方式实现
 *
 * @author duwenxu
 * @create 2021-04-02 17:05
 */
@Component
public class AscIIParamCodec implements ParamCodec {

    @Override
    public String decode(byte[] bytes, Object... objects) {
        return StrUtil.str(bytes, Charsets.UTF_8);
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        return StrUtil.bytes(value);
    }

    public static void main(String[] args) {
        byte[] bytes = {0x30, 0x35, 0x30, 0x33, 0x32, 0x36, 0x31, 0x31, 0x35, 0x39, 0x35, 0x30};
        AscIIParamCodec paramCodec = new AscIIParamCodec();
        String decode = paramCodec.decode(bytes);
        System.out.println(decode);
        byte[] encode = paramCodec.encode(decode);
        System.out.println(HexUtil.encodeHexStr(encode));
    }
}
