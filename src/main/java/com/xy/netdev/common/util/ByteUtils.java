package com.xy.netdev.common.util;

import cn.hutool.core.util.HexUtil;
import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        if (length <= 0){
            return null;
        }
        byte[] byteArray = new byte[length];
        System.arraycopy(bytes, start, byteArray, 0, length);
        return byteArray;
    }

    public static byte[] objToBytes(Object obj, int len){
        return objToBytes(obj, len, false);
    }

    /**
     * obj转字节数组
     * @param obj obj
     * @param len 长度
     * @param isFloat 是否浮点类型
     * @return 字节数组
     */
    public static byte[] objToBytes(Object obj, int len, boolean isFloat){
        byte [] data;
        if (len == 1){
            return new byte[]{Byte.parseByte(obj.toString())};
        }
        ByteOrder byteOrder = Unpooled.BIG_ENDIAN;
//        if (order != 1) {
//            byteOrder =  Unpooled.LITTLE_ENDIAN;
//        }
        switch(len){
            case 2:
                data = objToBytes(Short.parseShort(obj.toString()), byteOrder, Unpooled::copyShort);
                break;
            case 3:
                data = objToBytes(Integer.parseInt(obj.toString()), byteOrder, Unpooled::copyMedium);
                break;
            case 4:
                if (!isFloat){
                    data = objToBytes(Integer.parseInt(obj.toString()), byteOrder, Unpooled::copyInt);
                    break;
                }
                data = objToBytes(Integer.parseInt(obj.toString()), byteOrder, Unpooled::copyFloat);
                break;
            case 8:
                if (!isFloat){
                    data = objToBytes(Long.parseLong(obj.toString()), byteOrder, Unpooled::copyLong);
                    break;
                }
                data = objToBytes(Long.parseLong(obj.toString()), byteOrder, Unpooled::copyDouble);
                break;
            default:
                data = obj.toString().getBytes(Charset.forName("GB2312"));
                break;
        }
        return data;
    }


    /**
     * 字节数组转数字
     * @param bytes 原始字节数据
     * @param offset 起始位下标
     * @param length 长度
     * @return Number
     */
    public static Number byteToNumber(byte[] bytes, int offset, int length){
        return byteToNumber(bytes, offset, length, false, false);
    }

    /**
     * 字节数组转数字
     * @param bytes 原始字节数据
     * @param offset 起始位下标
     * @param length 长度
     * @param function 方法1
     * @param function1 方法2
     * @param isUnsigned 有无符号
     * @return
     */
    private static Number bytesToNum(byte[] bytes, int offset, int length, Function<ByteBuf, Number> function,
                                    Function<ByteBuf, Number> function1, boolean isUnsigned){
        if (!isUnsigned){
           return bytesToNum(bytes, offset, length, function);
        }
        return bytesToNum(bytes, offset, length, function1);
    }


    /**
     * 字节数组转数字
     * @param bytes 原始字节数据
     * @param offset 起始位下标
     * @param length 长度
     * @param isUnsigned 有无符号
     * @return Number
     */
    public static Number byteToNumber(byte[] bytes, int offset, int length,  boolean isUnsigned){
        return byteToNumber(bytes, offset, length, isUnsigned, false);
    }

    /**
     * 字节数组转数字
     * @param bytes 原始字节数据
     * @param offset 起始位下标
     * @param length 长度
     * @param isUnsigned 有无符号
     * @param isFloat 是否浮点类型
     * @return Number
     */
    public static Number byteToNumber(byte[] bytes, int offset, int length, boolean isUnsigned, boolean isFloat){
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
                if (!isFloat){
                    num = bytesToNum(bytes, offset, length, ByteBuf::readInt, ByteBuf::readUnsignedMedium, isUnsigned);
                    break;
                }
                num = at.favre.lib.bytes.Bytes.from(Objects.requireNonNull(byteArrayCopy(bytes, offset, length))).toFloat();
                break;
            case 8:
                if (!isFloat){
                    num = bytesToNum(bytes, offset, length, ByteBuf::readLong, ByteBuf::readUnsignedInt, isUnsigned);
                    break;
                }
                num = at.favre.lib.bytes.Bytes.from(Objects.requireNonNull(byteArrayCopy(bytes, offset, length))).toDouble();
                break;
            default:
                log.warn("转换失败, 数据长度{}不匹配", length);
                break;
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

    /**
     * 对象转换成字节数组。
     *
     * @param obj 字节数组。
     * @return 对象实例相应的序列化后的字节数组。
     * @throws IOException
     */
    public static byte[] objectToByte(Object obj) throws IOException {
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buff);
        out.writeObject(obj);
        try {
            return buff.toByteArray();
        } finally {
            out.close();
        }
    }

    /**
     * 序死化字节数组转换成实际对象。
     *
     * @param b 字节数组。
     * @return 对象。
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object byteToObject(byte[] b) throws IOException, ClassNotFoundException {
        ByteArrayInputStream buff = new ByteArrayInputStream(b);
        ObjectInputStream in = new ObjectInputStream(buff);
        Object obj = in.readObject();
        try {
            return obj;
        } finally {
            in.close();
        }
    }

    public static String numToHexStr(long num){
        return StringUtils.leftPad(HexUtil.toHex(num), 2,'0').toUpperCase();
    }


    /**
     * 累加字节数组取低位
     * @param bytes 原始数组
     * @param offset 起始位
     * @param len 长度
     * @return 低位
     */
    public static byte addGetBottom(byte[] bytes, int offset, int len){
        byte[] arrayCopy = byteArrayCopy(bytes, offset, len);
        int sum = 0;
        for (byte b : Objects.requireNonNull(arrayCopy)) {
            sum += (b & 0xFF);
        }
        return (byte)sum;
    }

}
