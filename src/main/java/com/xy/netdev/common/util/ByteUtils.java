package com.xy.netdev.common.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import java.nio.ByteOrder;
import java.util.List;
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

    public static byte xor(byte[] bytes, int offset, int len){
        byte[] data;
        if (bytes.length != len){
            data = byteArrayCopy(bytes, offset, len);
        }else {
            data = bytes;
        }
        byte temp = data[0];
        for(int i=1;i<data.length;i++){
            temp^=data[i];
        }
        return temp;
    }


    public static byte[] listToBytes(List<byte[]> list){
        return list.stream().reduce(new byte[]{}, com.google.common.primitives.Bytes::concat);
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


    public static byte[] objectToByte(Object obj, int len){
        byte [] data = {};
        if (len == 1){
            return new byte[]{(byte)obj};
        }
        ByteOrder byteOrder = Unpooled.BIG_ENDIAN;
//        if (order != 1) {
//            byteOrder =  Unpooled.LITTLE_ENDIAN;
//        } else {
//            byteOrder =  Unpooled.BIG_ENDIAN;
//        }
        switch(len){
            case 2:
                data = numToBytes((short)obj, byteOrder, Unpooled::copyShort);
                break;
            case 3:
                data = numToBytes((int)obj, byteOrder, Unpooled::copyMedium);
                break;
            case 4:
                data = numToBytes((int)obj, byteOrder, Unpooled::copyInt);
                break;
            case 8:
                data = numToBytes((long)obj, byteOrder, Unpooled::copyLong);
                break;
            default:break;
        }
        return data;
    }


    public static Number byteToNumber(byte[] bytes, int offset, int length){
        Number num = null;
        switch (length){
            case 1:
                num = bytesToNum(bytes, offset, length, ByteBuf::readByte);
                break;
            case 2:
                num = bytesToNum(bytes, offset, length, ByteBuf::readShort);
                break;
            case 3:
                num = bytesToNum(bytes, offset, length, ByteBuf::readMedium);
                break;
            case 4:
                num = bytesToNum(bytes, offset, length, ByteBuf::readInt);
                break;
            case 8:
                num = bytesToNum(bytes, offset, length, ByteBuf::readLong);
                break;
            default:break;
        }
        return num;
    }


    public static int byteToUnsignedInt(byte[] bytes){
        String hexStr = HexUtil.encodeHexStr(bytes);
        return Integer.parseUnsignedInt(hexStr, 16);
    }

    public static int byteToInt(byte[] bytes){
        String hexStr = HexUtil.encodeHexStr(bytes);
        return Integer.parseInt(hexStr, 16);
    }

    public static int byteToInt(byte byte1){
        return byteToInt(new byte[]{byte1});
    }

    public static long byteToLong(byte[] bytes){
        String hexStr = HexUtil.encodeHexStr(bytes);
        return Long.parseLong(hexStr, 16);
    }

    public static long byteToUnsignedLong(byte[] bytes){
        String hexStr = HexUtil.encodeHexStr(bytes);
        return Long.parseUnsignedLong(hexStr, 16);
    }

    public static long byteToLong(byte byte1){
        return byteToLong(new byte[]{byte1});
    }
}