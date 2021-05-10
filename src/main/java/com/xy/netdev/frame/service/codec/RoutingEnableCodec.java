package com.xy.netdev.frame.service.codec;


import cn.hutool.core.util.HexUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.service.ParamCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *路由表使能 编解码器
 */
@Slf4j
@Component
public class RoutingEnableCodec implements ParamCodec {

    @Override
    public String decode(byte[] bytes, Object... objects) {
        if (bytes.length>1){
            log.warn("参数[路由表使能]字节长度不正确,16进制字节数组值：{}",HexUtil.encodeHexStr(bytes));
        }
        String binaryStr = ByteUtils.byteToBinary(bytes[0]);
        return binaryStr;
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        return ByteUtils.objToBytes(Integer.parseInt(value, 2), 1);
    }
}
