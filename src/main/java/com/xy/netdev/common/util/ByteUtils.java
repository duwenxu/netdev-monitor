package com.xy.netdev.common.util;

import cn.hutool.core.util.HexUtil;
import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import java.nio.ByteOrder;
import java.util.function.Function;


@Slf4j
public class ByteUtils {


    public static String mD5(byte[] bytes){
        return DigestUtils.md5DigestAsHex(bytes);
    }

    public static long intToUnsignedLong(int i){
        return Long.parseUnsignedLong(HexUtil.toHex(i), 16);
    }

    public static long longToUnsignedLong(long i){
        return Long.parseUnsignedLong(HexUtil.toHex(i), 16);
    }

    public static <T> T bytesToNum(byte[] bytes, int offset, int length, Function<ByteBuf, T> function) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes, offset, length);
        T t = function.apply(byteBuf);
        ReferenceCountUtil.release(t);
        return t;
    }

    public static <T extends Number> byte[] numToBytes(T t, ByteOrder order, Function<T, ByteBuf> function){
        ByteBuf apply = function.apply(t);
        byte[] array = apply.array();
        if (order == Unpooled.LITTLE_ENDIAN){
            Bytes.reverse(array);
        }
        ReferenceCountUtil.release(apply);
        return array;
    }

    /**
     * 指定长度copy数组
     * @param bytes
     * @param start
     * @param length
     * @return
     */
    public static byte[] byteArrayCopy(byte[] bytes, int start, int length){
        byte[] byteArray = new byte[length];
        System.arraycopy(bytes, start, byteArray, 0, length);
        return byteArray;
    }
}
