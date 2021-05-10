package com.xy.netdev.frame.service.codec;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.frame.service.ParamCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 载波电平参数编解码器
 */
@Component
@Slf4j
public class ReceFreqCodec implements ParamCodec {

    private static final String sing1 = "FFFF";
    private static final String sign2 = "5555";

    @Override
    public String decode(byte[] bytes, Object... objects) {
        String hexStr = HexUtil.encodeHexStr(bytes);
        String value;
        switch (hexStr){
            case sing1:
                value = ">0"; break;
            case sign2:
                value = "<-60"; break;
            default:
                double v = Integer.parseInt(hexStr) * -0.1;
                value = v +"";
        }
        return value;
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        return new byte[0];
    }

}
