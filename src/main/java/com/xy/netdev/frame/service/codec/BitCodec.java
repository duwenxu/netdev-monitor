package com.xy.netdev.frame.service.codec;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.service.ParamCodec;
import com.xy.netdev.frame.service.modemscmm.ModemScmmInterPrtcServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 位参数转换
 *
 * @author duwenxu
 * @create 2021-04-06 10:34
 */
@Component
@Slf4j
public class BitCodec implements ParamCodec {
    @Autowired
    private ModemScmmInterPrtcServiceImpl service;

    @Override
    public String decode(byte[] bytes, Object... objects) {
        int start = (int) objects[0];
        int point = (int) objects[1];
        return service.bitStrByPoint(bytes[0], start, point);
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        int start = (int) objects[0];
        int point = (int) objects[1];
        if (value.length() != point) {
            log.error("发送位编码长度错误，要编码的值：{}，配置的长度：{}", value, point);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(value);
        while (start > 0) {
            sb = new StringBuilder().append('0').append(sb);
            start--;
        }
        while (sb.length() < 8) {
            sb.append('0');
        }
        return ByteUtils.objToBytes(Integer.parseInt(sb.toString(), 2), 2);
    }
}
