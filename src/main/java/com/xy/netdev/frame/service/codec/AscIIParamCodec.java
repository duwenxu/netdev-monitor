package com.xy.netdev.frame.service.codec;

import cn.hutool.core.util.StrUtil;
import com.google.common.base.Charsets;
import com.xy.netdev.frame.service.ParamCodec;

/**
 * ASCII码编解码方式实现
 *
 * @author duwenxu
 * @create 2021-04-02 17:05
 */
public class AscIIParamCodec implements ParamCodec {

    @Override
    public String decode(byte[] bytes, Object... objects) {
        return StrUtil.str(bytes, Charsets.UTF_8);
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        return new byte[0];
    }
}
