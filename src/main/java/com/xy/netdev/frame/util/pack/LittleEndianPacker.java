package com.xy.netdev.frame.util.pack;

import java.math.BigDecimal;


/**
 * @author Administrator
 * @date 2019/9/30
 */
public class LittleEndianPacker implements IPacker{

    private LittleEndianPacker(){}

    private static class SingletonInstance{
        private static final LittleEndianPacker INSTANCE = new LittleEndianPacker();
    }

    public static LittleEndianPacker getInstance(){
        return SingletonInstance.INSTANCE;
    }

    @Override
    public short getUnsignedChar(byte[] bytes) {
        return (short)(0xFF & bytes[0]);
    }

    @Override
    public short getShort(byte[] bytes) {
        return (short)((0xFF & bytes[0])
                | (0xFF & bytes[1]) << 8);
    }

    @Override
    public int getUnsignedShort(byte[] bytes) {
        return ((0xFF & bytes[0])
                | (0xFF & bytes[1]) << 8);
    }

    @Override
    public int getInt(byte[] bytes) {
        return ((0xFF & bytes[0])
                | ((0xFF & bytes[1]) << 8)
                | ((0xFF & bytes[2]) << 16)
                | ((0xFF & bytes[3]) << 24));
    }

    @Override
    public long getUnsignedInt(byte[] bytes) {
        return ((0xFFL & bytes[0])
                | ((0xFFL & bytes[1]) << 8)
                | ((0xFFL & bytes[2]) << 16)
                | ((0xFFL & bytes[3]) << 24));
    }

    @Override
    public long getLong(byte[] bytes) {
        return getaLong(bytes);
    }

    @Override
    public BigDecimal getUnsignedLong(byte[] bytes) {
        long value = ((0xFFL & bytes[0])
                | ((0xFFL & bytes[1]) << 8)
                | ((0xFFL & bytes[2]) << 16)
                | ((0xFFL & bytes[3]) << 24)
                | ((0xFFL & bytes[4]) << 32)
                | ((0xFFL & bytes[5]) << 40)
                | ((0xFFL & bytes[6]) << 48)
                | ((0xFFL & bytes[7]) << 56));
        if (value > 0) {
            return new BigDecimal(value);
        }
        long lowValue = value & 0x7FFFFFFFFFFFFFFFL;
        return BigDecimal.valueOf(lowValue).add(BigDecimal.valueOf(Long.MAX_VALUE)).add(BigDecimal.valueOf(1));
    }

    private long getaLong(byte[] bytes) {
        return (0xFFL & bytes[0])
                | ((0xFFL & bytes[1]) << 8)
                | ((0xFFL & bytes[2]) << 16)
                | ((0xFFL & bytes[3]) << 24)
                | ((0xFFL & bytes[4]) << 32)
                | ((0xFFL & bytes[5]) << 40)
                | ((0xFFL & bytes[6]) << 48)
                | ((0xFFL & bytes[7]) << 56);
    }
}
