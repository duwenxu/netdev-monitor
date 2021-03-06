package com.xy.netdev.common.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;


@Slf4j
public class ByteUtils {

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
        byte temp = Objects.requireNonNull(data)[0];
        for(int i=1;i<data.length;i++){
            temp^=data[i];
        }
        return temp;
    }


    public static byte[] listToBytes(List<byte[]> list){
        return list.stream().reduce(new byte[]{}, com.google.common.primitives.Bytes::concat);
    }

    /**
     * ????????????copy??????
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
     * obj???????????????
     * @param obj obj
     * @param len ??????
     * @param isFloat ??????????????????
     * @return ????????????
     */
    public static byte[] objToBytes(Object obj, int len, boolean isFloat){
        byte [] data;
        if (len == 1){
            return new byte[]{(byte)Integer.parseInt(obj.toString())};
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
                    data = objToBytes((int)Long.parseLong(obj.toString()), byteOrder, Unpooled::copyInt);
                    break;
                }
                data = objToBytes(Float.parseFloat(obj.toString()), byteOrder, Unpooled::copyFloat);
                break;
            case 8:
                if (!isFloat){
                    data = objToBytes(Long.parseLong(obj.toString()), byteOrder, Unpooled::copyLong);
                    break;
                }
                data = objToBytes(Double.parseDouble(obj.toString()), byteOrder, Unpooled::copyDouble);
                break;
            default:
                data = obj.toString().getBytes(Charset.forName("GB2312"));
//                int dataLen = data.length;
//                List<byte[]> dataList = new ArrayList<>();
//                if(dataLen<len){
//                    for (int i = 0; i < len-dataLen ; i++) {
//                        byte[] val = {(byte)0x00};
//                        dataList.add(val);
//                    }
//                }
//                dataList.add(data);
//                data = listToBytes(dataList);
                break;
        }
        return data;
    }


    /**
     * ?????????????????????
     * @param bytes ??????????????????
     * @param offset ???????????????
     * @param length ??????
     * @return Number
     */
    public static Number byteToNumber(byte[] bytes, int offset, int length){
        return byteToNumber(bytes, offset, length, false, false);
    }

    /**
     * ?????????????????????
     * @param bytes ??????????????????
     * @param offset ???????????????
     * @param length ??????
     * @param function ??????1
     * @param function1 ??????2
     * @param isUnsigned ????????????
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
     * ?????????????????????
     * @param bytes ??????????????????
     * @param offset ???????????????
     * @param length ??????
     * @param isUnsigned ????????????
     * @return Number
     */
    public static Number byteToNumber(byte[] bytes, int offset, int length,  boolean isUnsigned){
        return byteToNumber(bytes, offset, length, isUnsigned, false);
    }

    /**
     * ?????????????????????
     * @param bytes ??????????????????
     * @param offset ???????????????
     * @param length ??????
     * @param isUnsigned ????????????
     * @param isFloat ??????????????????
     * @return Number
     */
    public static Number byteToNumber(byte[] bytes, int offset, int length, boolean isUnsigned, boolean isFloat){
        Number num = null;
        switch (length){
            case 1:
                num = bytesToNum(bytes, offset, length, ByteBuf::readByte, ByteBuf::readUnsignedByte, isUnsigned);
                break;
            case 2:
                num = bytesToNum(bytes, offset, length, ByteBuf::readShort, ByteBuf::readUnsignedShort, isUnsigned);
                break;
            case 3:
                num = bytesToNum(bytes, offset, length, ByteBuf::readMedium, ByteBuf::readUnsignedMedium, isUnsigned);
                break;
            case 4:
                if (!isFloat){
                    num = bytesToNum(bytes, offset, length, ByteBuf::readInt, ByteBuf::readUnsignedInt, isUnsigned);
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
                break;
        }
        return num;
    }

    public static int byteToUnsignedInt(byte[] bytes){
        String hexStr = HexUtil.encodeHexStr(bytes);
        return Integer.parseUnsignedInt(hexStr, 16);
    }

    public static int byteToInt(byte[] bytes){
        if (bytes==null||bytes.length == 0){
            return 0;
        }
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
     * ??????????????????????????????
     *
     * @param obj ???????????????
     * @return ???????????????????????????????????????????????????
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
     * ?????????????????????????????????????????????
     *
     * @param b ???????????????
     * @return ?????????
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

    /**
     * ???????????????????????????????????????
     * @param num
     * @return
     */
    public static String numToHexStr(long num){
        return StringUtils.leftPad(HexUtil.toHex(num), 2,'0').toUpperCase();
    }


    /**
     * ???????????????????????????
     * @param bytes ????????????
     * @param offset ?????????
     * @param len ??????
     * @return ??????
     */
    public static byte addGetBottom(byte[] bytes, int offset, int len){
        byte[] arrayCopy = byteArrayCopy(bytes, offset, len);
        int sum = 0;
        for (byte b : Objects.requireNonNull(arrayCopy)) {
            sum += (b & 0xFF);
        }
        return (byte)sum;
    }


    /**
     * byte?????????????????????
     * @param b ??????
     * @param length ??????
     * @return ??????????????????
     */
    public static String byteToBinary(byte b, int length){
        int temp = b & 0xff;
        String string = Integer.toBinaryString(temp);
        StringBuilder stringBuilder = new StringBuilder();
        int a = string.length();
        while (a < length){
            stringBuilder.append("0");
            a++;
        }
        stringBuilder.append(string);
        return stringBuilder.toString();
    }

    public static String byteToBinary(byte b){
        return byteToBinary(b, 8);
    }

    /**
     * ????????????byte[]
     * @param bytes1 ??????1
     * @param bytes2 ??????2
     * @return ???????????????
     */
    public static byte[] bytesMerge(byte[] bytes1,byte[] bytes2){
        byte[] bytes = new byte[bytes1.length + bytes2.length];
        System.arraycopy(bytes1, 0, bytes, 0, bytes1.length);
        System.arraycopy(bytes2, 0, bytes, bytes1.length, bytes2.length);
        return bytes;
    }

    /**
     * ????????????
     * @param bytes byte??????
     * @param pairs ????????????
     * @return ????????????
     */
    @SafeVarargs
    public static byte[] byteReplace(byte[] bytes, Pair<String, String>... pairs){
        if (ArrayUtil.isEmpty(pairs)){
            return bytes;
        }
        return byteReplace(bytes, pairs[0].getKey().length(), pairs);
    }

    /**
     * ????????????
     * @param bytes byte??????
     * @param offset ?????????
     * @param len ??????
     * @param pairs ????????????
     * @return ????????????
     */
    @SafeVarargs
    public static byte[] byteReplace(byte[] bytes, int offset, int len, Pair<String, String>... pairs){
        if (len == bytes.length){
            return byteReplace(bytes, pairs);
        }
        byte[] beginBytes = null;
        if (offset != 0){
            beginBytes = byteArrayCopy(bytes, 0, offset);
        }
        byte[] checkBytes = byteArrayCopy(bytes, offset, len - offset);
        byte[] endBytes = byteArrayCopy(bytes, len, bytes.length - len);
        byte[] resultBytes = byteReplace(checkBytes, pairs);
        if (beginBytes != null && endBytes != null){
            return Bytes.concat(beginBytes, resultBytes, endBytes);
        }
        if (beginBytes != null){
            return Bytes.concat(beginBytes, resultBytes);
        }
        if (endBytes != null){
            return Bytes.concat(resultBytes, endBytes);
        }
        return resultBytes;
    }


    /**
     * ????????????
     * @param bytes byte??????
     * @param skip ??????
     * @param pairs ???????????????
     * @return ???????????????
     */
    @SafeVarargs
    public static byte[] byteReplace(byte[] bytes, int skip, Pair<String, String>... pairs){
        if (ArrayUtil.isEmpty(pairs)){
            return bytes;
        }
        for (Pair<String, String> pair : pairs) {
            Assert.isTrue(pair.getKey().length() == skip);
        }
        StringBuilder stringBuilder = new StringBuilder();
        int skipSize = skip / 2;
        for (int i = 0; i < bytes.length; i = i + skipSize) {
            if (( i + skipSize )> bytes.length){
                byte[] bytesTemp = new byte[bytes.length - i];
                System.arraycopy(bytes, i, bytesTemp, 0, bytes.length - i);
                stringBuilder.append(HexUtil.encodeHexStr(bytesTemp));
                break;
            }
            byte[] bytesTemp = new byte[skipSize];
            System.arraycopy(bytes, i, bytesTemp, 0, skipSize);
            String encodeHexStr = HexUtil.encodeHexStr(bytesTemp);
            boolean ifReplace = false;
            for (Pair<String, String> pair : pairs) {
                if (pair.getKey().equalsIgnoreCase(encodeHexStr)) {
                    stringBuilder.append(pair.getValue());
                    ifReplace = true;
                    break;
                }
            }
            if (!ifReplace){
                stringBuilder.append(encodeHexStr);
            }
        }
        return HexUtil.decodeHex(stringBuilder.toString());
    }

    /**
     * ???????????????????????????????????????
     * @param num
     * @return
     */
    public static String lefPadNumToHexStr(long num){
        return StringUtils.leftPad(HexUtil.toHex(num), 2,'0').toUpperCase();
    }

    /**
     * ???????????????????????????0
     * @param num
     * @return
     */
    public static String make0HexStr(String num){
        return StringUtils.leftPad(num, 2,'0')+"H".toUpperCase();
    }

    // ???????????????
    public byte[] checkCodeMsg(byte[] src) {
        byte[] rv = new byte[src.length + 1];
        int i;
        int myxor;
        System.arraycopy(src, 0, rv, 0, src.length);
        myxor = src[0];
        for (i = 1; i < src.length; i++) {
            myxor ^= src[i];
        }
        rv[src.length] = (byte) myxor;
        return rv;
    }

    //?????????????????????
    public static byte[] enCode(byte[] src) {
        byte[] rv = null;
        int i = 0;
        ArrayList<Byte> al = new ArrayList<Byte>(src.length + 32);

        for (i = 0; i < src.length; i++) {
            if (src[i] == 0x7E) {
                al.add((byte) 0x7D);
                al.add((byte) 0x5E);
            } else if (src[i] == 0x7D) {
                al.add((byte) 0x7D);
                al.add((byte) 0x5D);
            } else {
                al.add(src[i]);
            }
        }

        rv = new byte[al.size()];
        for (i = 0; i < al.size(); i++) {
            rv[i] = al.get(i);
        }
        return rv;
    }

    // ?????????????????????7E
    public byte[] packagingMsg(byte[] src) {
        byte[] rv = new byte[src.length + 2];
        System.arraycopy(src, 0, rv, 1, src.length);
        rv[0] = 0x7E;
        rv[rv.length - 1] = 0x7E;
        return rv;
    }

    /**
     * ??????byte?????????int????????????
     * @return
     * @note ?????????????????????4???????????????????????????????????????bytes???????????????????????????????????????int
     */
    public static int Bytes2Int_BE(byte[] bytes){
        if(bytes.length == 4 || bytes.length ==2){
            int iRst = (bytes[0] << 24) & 0xFF;
            iRst |= (bytes[1] << 16) & 0xFF;
            if(bytes.length == 4){
                iRst |= (bytes[2] << 8) & 0xFF;
                iRst |= bytes[3] & 0xFF;
            }
            return iRst;
        }
        return -1;
    }

    public static int getInt(byte[] bytes){
        return (0xff & bytes[0]) | (0xff00 & (bytes[1]<<8)) | (0xff0000 & (bytes[2]<<16)) | (0xff000000 & (bytes[3]<<24));
    }

    public static void main(String[] args) {
//        String str = "7e138303000e7d5ef00056322e302d313930393233607e";
////        byte[] bytes = HexUtil.decodeHex(str);
////        byte[] byteReplace = byteReplace(bytes, 1, bytes.length - 1, Pair.of("7E", "7D5E"), Pair.of("7D", "7D5D"));
////        System.out.println(HexUtil.encodeHexStr(byteReplace).toUpperCase());
//        byte[] bytes = HexUtil.decodeHex(str);
//        byte[] replace = byteReplace(bytes, Pair.of("7D5E", "7E"), Pair.of("7D5D", "7D"));
//        System.out.println(HexUtil.encodeHexStr(replace).toUpperCase());

        byte[] bytes = new byte[]{(byte) 0xC3,(byte) 0xF5,(byte) 0xDA,(byte) 0x42};
        String s = byteToNumber(bytes, 0, 4, false, true).toString();
        System.out.println(s);
    }
}
