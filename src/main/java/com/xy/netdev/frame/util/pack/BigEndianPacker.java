package com.xy.netdev.frame.util.pack;

import java.math.BigDecimal;


/**
 * @author Administrator
 * @date 2019/9/30
 */
public class BigEndianPacker implements IPacker{

    private BigEndianPacker(){}

    private static class SingletonInstance{
        private static final BigEndianPacker INSTANCE = new BigEndianPacker();
    }

    public static BigEndianPacker getInstance(){
        return SingletonInstance.INSTANCE;
    }

    @Override
    public short getUnsignedChar(byte[] bytes) {
        return (short)(0xFF & bytes[0]);
    }

    @Override
    public short getShort(byte[] bytes) {
        return (short)((0xFF & bytes[1])
                | (0xFF & bytes[0]) << 8);
    }

    @Override
    public int getUnsignedShort(byte[] bytes) {
        return ((0xFF & bytes[1])
                | (0xFF & bytes[0]) << 8);
    }

    @Override
    public int getInt(byte[] bytes) {
        return ((0xFF & bytes[3])
                | ((0xFF & bytes[2]) << 8)
                | ((0xFF & bytes[1]) << 16)
                | ((0xFF & bytes[0]) << 24));
    }

    @Override
    public long getUnsignedInt(byte[] bytes) {
        return ((0xFFL & bytes[3])
                | ((0xFFL & bytes[2]) << 8)
                | ((0xFFL & bytes[1]) << 16)
                | ((0xFFL & bytes[0]) << 24));
    }

    @Override
    public long getLong(byte[] bytes) {
        return ((0xFFL & bytes[7])
                | ((0xFFL & bytes[6]) << 8)
                | ((0xFFL & bytes[5]) << 16)
                | ((0xFFL & bytes[4]) << 24)
                | ((0xFFL & bytes[3]) << 32)
                | ((0xFFL & bytes[2]) << 40)
                | ((0xFFL & bytes[1]) << 48)
                | ((0xFFL & bytes[0]) << 56));
    }

    @Override
    public BigDecimal getUnsignedLong(byte[] bytes) {
        long value = (0xFFL & bytes[7])
                | ((0xFFL & bytes[6]) << 8)
                | ((0xFFL & bytes[5]) << 16)
                | ((0xFFL & bytes[4]) << 24)
                | ((0xFFL & bytes[3]) << 32)
                | ((0xFFL & bytes[2]) << 40)
                | ((0xFFL & bytes[1]) << 48)
                | ((0xFFL & bytes[0]) << 56);

        long lowValue = value & 0x7FFFFFFFFFFFFFFFL;
        return BigDecimal.valueOf(lowValue).add(BigDecimal.valueOf(Long.MAX_VALUE)).add(BigDecimal.valueOf(1));
    }
}
