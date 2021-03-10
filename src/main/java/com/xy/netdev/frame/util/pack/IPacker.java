package com.xy.netdev.frame.util.pack;

import java.math.BigDecimal;

/**
 * @author Administrator
 * @date 2019/9/30
 */
public interface IPacker {
    short getUnsignedChar(byte[] bytes);
    short getShort(byte[] bytes);

    int getUnsignedShort(byte[] bytes);

    int getInt(byte[] bytes);

    long getUnsignedInt(byte[] bytes);

    long getLong(byte[] bytes);

    BigDecimal getUnsignedLong(byte[] bytes);
}
