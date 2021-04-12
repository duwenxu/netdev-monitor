package com.xy.netdev.frame.service.codec;

import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.service.ParamCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.xy.netdev.common.util.ByteUtils.byteToBinary;

/**
 * 位参数转换
 *
 * @author duwenxu
 * @create 2021-04-06 10:34
 */
@Component
@Slf4j
public class BitCodec implements ParamCodec {

    @Override
    public String decode(byte[] bytes, Object... objects) {
        int start = (int) objects[0];
        int point = (int) objects[1];
        return bitStrByPoint(bytes[0], start, point);
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

    /**
     * 获取byte中指定bit的字符串
     *
     * @param byt   字节
     * @param start 起始位置
     * @param range 长度范围
     * @return bit字符串
     */
    public String bitStrByPoint(byte byt, int start, int range) {
        if (start > 7 || range > 8) {
            log.warn("输入bit范围错误：起始位置:{}.长度：{}", start, range);
        }
        return byteToBinary(byt).substring(start, start + range);
    }
}
