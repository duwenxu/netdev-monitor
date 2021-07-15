package com.xy.netdev.frame.service.codec;

import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.service.ParamCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @Desc  400W短波设备参数转换
 *        eg:收频率和发频率：4字节中最高字节为频率小数点前的值，后三个字节为小数点后的值。例如：频率20.75000为0x140124F8
 * @Author 嗜雪的蚂蚁
 * @Date 2021/7/13 15:51
 **/
@Component
@Slf4j
public class ShortWaveFreqCodec implements ParamCodec {

    @Override
    public String decode(byte[] bytes, Object... objects) {
        Assert.isTrue(bytes.length == 4,"400W短波-收/发频率参数长度异常，长度为:"+bytes.length);
        byte[] headBytes = ByteUtils.byteArrayCopy(bytes, 0, 1);
        byte[] tailBytes = ByteUtils.byteArrayCopy(bytes, 1, 3);
        int head = ByteUtils.byteToInt(headBytes);
        int tail = ByteUtils.byteToInt(tailBytes);
        return head + "." + tail;
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        return new byte[0];
    }

    public static void main(String[] args) {
        byte[] bytes = {0x14, 0x01, 0x24, (byte) 0xF8};
        ShortWaveFreqCodec shortWaveFreqCodec = new ShortWaveFreqCodec();
        String decode = shortWaveFreqCodec.decode(bytes);
        System.out.println(decode);
    }
}
