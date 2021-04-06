package com.xy.netdev.frame.service;

/**
 * @Description: 参数转换方法接口
 * @Date 17:00 2021/4/2
 * @Author duwx
 */
public interface ParamCodec {

    /**
     * 参数解码 byte[]转String值
     *
     * @param bytes   字节数组
     * @param objects 可能需要的其他传入参数
     * @return 解码值
     */
    String decode(byte[] bytes, Object... objects);

    /**
     * 参数编码 String值转byte[]
     *
     * @param value   编码值
     * @param objects 可能需要的其他传入参数
     * @return 编码的byte[]
     */
    byte[] encode(String value, Object... objects);
}
