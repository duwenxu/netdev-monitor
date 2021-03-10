package com.xy.netdev.frame.util.pack;

import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ReferenceCountUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 解包数据处理
 * @author Administrator
 * @date 2019/9/30
 */
public class Packer {
    private static final LinkedHashMap<String, PackFmt> ENUM_MAP = EnumUtil.getEnumMap(PackFmt.class);

    public static final PooledByteBufAllocator BUF_ALLOCATOR = new PooledByteBufAllocator();

    public static List<Object> unpack(String fmt, byte[] bytes) {
        return unpack(fmt, bytes, true);
    }

    public static byte[] pack(String fmt, List<Object> list) {
        return pack(fmt, list, true);
    }

    /**
     * 指定大小端解byte[]
     * @param fmt : 需要的数据类型格式
     * @param bytes : 原始的字节内容
     * @param isLittleEndian : 大小端,true:小端,false:大端
     * @return
     */
    public static List<Object> unpack(String fmt, byte[] bytes, boolean isLittleEndian) {
        if (fmt.length() <=0) {
            return null;
        }
        IPacker packer;
        if (isLittleEndian) {
            packer = LittleEndianPacker.getInstance();
        } else {
            packer = BigEndianPacker.getInstance();
        }
        List<Object> list = new ArrayList<>();
        char[] chars = fmt.toCharArray();
        int length = bytes.length;
        int begin = 0;
        for(char c : chars) {
            if (begin >= length) {
                break;
            }
            boolean upperCase = Character.isUpperCase(c);
            PackFmt packFmtTemp = ENUM_MAP.get(String.valueOf(c));
            if (packFmtTemp !=null){
                int v = packFmtTemp.v();
                switch (v){
                    case 1:
                        list.add(packer.getUnsignedChar(subBytes(bytes, begin,  v)));
                        break;
                    case 2:
                        list.add(upperCase ? packer.getUnsignedShort(subBytes(bytes, begin, v))
                                : packer.getShort(subBytes(bytes, begin, v)));
                        break;
                    case 4:
                        list.add(upperCase ? packer.getUnsignedInt(subBytes(bytes, begin, v))
                                : packer.getInt(subBytes(bytes, begin, v)));
                        break;
                    default:
                        list.add(upperCase ? packer.getUnsignedLong(subBytes(bytes, begin, v))
                                : packer.getLong(subBytes(bytes, begin, v)));
                        break;
                }
                begin += v;
            }
        }
        return list;
    }

    /**
     * 指定大小端压缩数据
     * @param fmt : 需要的数据类型格式
     * @param bytes : 原始的字节内容
     * @param isLittleEndian : 大小端,true:小端,false:大端
     * @return
     */
    public static byte[] pack(String fmt, List<Object> list, boolean isLittleEndian){
        if (StrUtil.isBlank(fmt)) {
            return null;
        }
        try {
            List<byte[]> listTemp = new ArrayList<>();
            char[] chars = fmt.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                ByteBuf byteBuf = BUF_ALLOCATOR.ioBuffer(8);
                char aChar = chars[i];
                PackFmt packFmtTemp = ENUM_MAP.get(String.valueOf(aChar));
                int v = packFmtTemp.v();
                if (v >= 4) {
                    long aLong = Long.parseLong(list.get(i).toString());
                    if (!isLittleEndian) {
                        byteBuf.writeLong(aLong);
                        listTemp.add(ByteBufUtil.getBytes(byteBuf, 8 - v, v));
                    } else {
                        byteBuf.writeLongLE(aLong);
                        listTemp.add(ByteBufUtil.getBytes(byteBuf, 0, v));
                    }

                } else if (v == 2) {
                    int integer = Integer.parseInt(list.get(i).toString());
                    if (!isLittleEndian) {
                        byteBuf.writeInt(integer);
                        listTemp.add(ByteBufUtil.getBytes(byteBuf, 4 - v, v));
                    } else {
                        byteBuf.writeIntLE(integer);
                        listTemp.add(ByteBufUtil.getBytes(byteBuf, 0, v));
                    }

                } else {
                    int integer = Integer.parseInt(list.get(i).toString());
                    byteBuf.writeInt(integer);
                    listTemp.add(ByteBufUtil.getBytes(byteBuf, 4 - v, v));
                }
                ReferenceCountUtil.release(byteBuf);
            }
            return listTemp.stream().reduce(new byte[0], Bytes::concat);
        }catch(Exception e) {

        }
        return null;
    }

    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        if (count > 0) {
            System.arraycopy(src, begin, bs, 0, count);
        }
        return bs;
    }

    public static char[] byteToBitChars(byte[] bs) {
        String v = "";
        for (byte b : bs) {
            for(int i = 7; i >= 0; i--) {
                v = v + ((byte)((b >> i) & 0x1));
            }
        }
        return v.toCharArray();
    }

    /**
     * 将2进制字符转化为10进制int
     * @param bs eg:0101
     * @return
     */
    public static int binaryStr2Int10(String bs) {
        int len = bs.length();
        int value = 0;
        int tmp,  max = len - 1;
        for (int i = 0; i < len; ++i) {
            tmp = bs.charAt(i) - '0';
            value += tmp * Math.pow(2, max--);
        }
        return value;
    }

    public static int shortToInt(short v) {
        return (v & 0x000000FF) |(v & 0x0000FF00);
    }

    public static long intToLong(int v) {
        return (v & 0x00000000000000FF) | (v & 0x000000000000FF00)  | (v & 0x0000000000FF0000) | (v & 0x00000000FF000000);
    }

    public static long o2Long(Object v) {
        return Long.parseLong(String.valueOf(v));
    }

    public static int o2Integer(Object v) {
        return Integer.parseInt(String.valueOf(v));
    }
}
