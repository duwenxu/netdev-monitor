package com.xy.netdev.common.util;

import cn.hutool.core.util.HexUtil;
import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
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

    public static <T extends Number> byte[] objToBytes(T t, ByteOrder order, Function<T, ByteBuf> function){
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


    public static byte[] objToBytes(Object obj, int len){
        byte [] data;
        if (len == 1){
            return new byte[]{Byte.parseByte(obj.toString())};
        }
        ByteOrder byteOrder = Unpooled.BIG_ENDIAN;
//        if (order != 1) {
//            byteOrder =  Unpooled.LITTLE_ENDIAN;
//        } else {
//            byteOrder =  Unpooled.BIG_ENDIAN;
//        }
        switch(len){
            case 2:
                data = objToBytes(Short.parseShort(obj.toString()), byteOrder, Unpooled::copyShort);
                break;
            case 3:
                data = objToBytes(Integer.parseInt(obj.toString()), byteOrder, Unpooled::copyMedium);
                break;
            case 4:
                data = objToBytes(Integer.parseInt(obj.toString()), byteOrder, Unpooled::copyInt);
                break;
            case 8:
                data = objToBytes(Long.parseLong(obj.toString()), byteOrder, Unpooled::copyLong);
                break;
            default:
                data = obj.toString().getBytes(Charset.forName("GB2312"));
                break;
        }
        return data;
    }


    public static Number byteToNumber(byte[] bytes, int offset, int length){
        return byteToNumber(bytes, offset, length, false);
    }

    public static Number bytesToNum(byte[] bytes, int offset, int length, Function<ByteBuf, Number> function,
                                    Function<ByteBuf, Number> function1, boolean isUnsigned){
        if (!isUnsigned){
           return bytesToNum(bytes, offset, length, function);
        }
        return bytesToNum(bytes, offset, length, function1);
    }

    public static Number byteToNumber(byte[] bytes, int offset, int length, boolean isUnsigned){
        Number num = null;
        switch (length){
            case 1:
                num = bytesToNum(bytes, offset, length, ByteBuf::readByte);
                break;
            case 2:
                num = bytesToNum(bytes, offset, length, ByteBuf::readShort, ByteBuf::readUnsignedByte, isUnsigned);
                break;
            case 3:
                num = bytesToNum(bytes, offset, length, ByteBuf::readMedium, ByteBuf::readUnsignedShort, isUnsigned);
                break;
            case 4:
                num = bytesToNum(bytes, offset, length, ByteBuf::readInt, ByteBuf::readUnsignedMedium, isUnsigned);
                break;
            case 8:
                num = bytesToNum(bytes, offset, length, ByteBuf::readLong, ByteBuf::readUnsignedInt, isUnsigned);
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

    public static byte[] placeholderByte(int size){
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(0);
        }
        return Bytes.toArray(list);
    }

}
