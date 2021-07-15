package com.xy.netdev.frame.service.codec.modem;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.factory.SingletonFactory;
import com.xy.netdev.frame.service.ParamCodec;
import com.xy.netdev.frame.service.codec.Hex2DecParamCodec;
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
    private final Hex2DecParamCodec hex2DecParamCodec;

    public ReceFreqCodec() {
        this.hex2DecParamCodec = SingletonFactory.getInstance(Hex2DecParamCodec.class);
    }

    @Override
    public String decode(byte[] bytes, Object... objects) {
        String hexStr = hex2DecParamCodec.decode(bytes,"",2);
        String value = null;
        switch (hexStr){
            case sing1:
                value = ">0"; break;
            case sign2:
                value = "<-60"; break;
            default:
                try {
                    double v = Double.parseDouble(hexStr) * -0.1;
                    value = v +"";
                } catch (NumberFormatException e) {
                   log.error("收载波电平参数解析失败：string值：{}",hexStr);
                }
        }
        return value;
    }

    /**
     * 只读参数 不需要实现其编码函数
     * @param value   编码值
     * @param objects 可能需要的其他传入参数
     * @return
     */
    @Override
    public byte[] encode(String value, Object... objects) {
        return new byte[0];
    }

}
