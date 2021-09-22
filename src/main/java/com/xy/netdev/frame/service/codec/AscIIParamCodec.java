package com.xy.netdev.frame.service.codec;

import cn.hutool.core.text.ASCIIStrCache;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.Ascii;
import com.google.common.base.Charsets;
import com.xy.netdev.frame.service.ParamCodec;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.xy.netdev.common.util.ByteUtils.byteToInt;

/**
 * ASCII码编解码方式实现
 *
 * @author duwenxu
 * @create 2021-04-02 17:05
 */
@Component
public class AscIIParamCodec implements ParamCodec {

    @Override
    public String decode(byte[] bytes, Object... objects) {
        return StrUtil.str(bytes, Charsets.UTF_8);
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        return StrUtil.bytes(value);
    }

    public static void main(String[] args) {
        //15 41 AC 68 03 93 0D 0A
//        38:5a:32:37:32:2d:31:31:20:53:44:48:e8:ae:be:e5:a4:87
//        1541586803670d0a
//        byte[] bytes = {0x7B, 0x41, 0x30, 0x68, 0x7D, 0x55};
//        byte[] bytes1 = {0x38, 0x5a, 0x32, 0x37, 0x32, 0x2d, 0x31, 0x31, 0x20, 0x53, 0x44, 0x48, (byte) 0xe8, (byte) 0xae, (byte)0xbe, (byte)0xe5,(byte)0xa4,(byte)0x87};
        byte[] bytes2 = {0x36, (byte) 0xc0, (byte) 0x04,0x02};
        AscIIParamCodec paramCodec = new AscIIParamCodec();
//        String decode1 = new String(bytes);
        String decode = paramCodec.decode(bytes2);
//        System.out.println(decode1+"222");
        System.out.println(decode);
        byte[] encode = paramCodec.encode(decode);
        System.out.println(HexUtil.encodeHexStr(encode));

        //16禁止字节转ASCii
//        String str = StrUtil.str(new byte[]{0x31}, StandardCharsets.UTF_8);
//        String s = new String(new byte[]{0x31});
//        System.out.println("ASCII码1："+ str);
//        System.out.println("ASCII码2："+ s);

//        System.out.println(StrUtil.str(bytes1,StandardCharsets.UTF_8));

    }
}
