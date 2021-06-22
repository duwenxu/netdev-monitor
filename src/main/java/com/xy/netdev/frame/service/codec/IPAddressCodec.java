package com.xy.netdev.frame.service.codec;

import cn.hutool.core.util.HexUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.service.ParamCodec;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.net.InetAddress;

/**
 * 16进制与IP地址字符串转换
 *
 * @author duwenxu
 * @create 2021-04-09 14:08
 */
@Component
public class IPAddressCodec implements ParamCodec {

    @SneakyThrows
    @Override
    public String decode(byte[] bytes, Object... objects) {
        //这里可能会表示多个IP地址
        StringBuilder fullIpStr = new StringBuilder();
        for (int i = 0; i < bytes.length;) {
            byte[] oneBytes = new byte[4];
            System.arraycopy(bytes, i, oneBytes, 0, 4);
            i = i + 4;
            String hexStr = HexUtil.encodeHexStr(oneBytes);
            String ipStr = InetAddress.getByAddress(DatatypeConverter.parseHexBinary(hexStr)).toString().substring(1);
            fullIpStr.append(ipStr);
            if (i < bytes.length) {
                fullIpStr.append("\n");
            }
        }
        return fullIpStr.toString();
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        String[] ipStrs = value.split(" ");
        byte[] fullBytes = new byte[]{};
        for (String ipStr : ipStrs) {
            String[] ipStrParts = ipStr.split("\\.");
            byte[] bytes = new byte[4];
            for (int i = 0; i < 4; i++) {
                int part = Integer.parseInt(ipStrParts[i]);
                bytes[i] = (byte) part;
            }
            fullBytes = ByteUtils.bytesMerge(fullBytes, bytes);
        }
        return fullBytes;
    }

    public static void main(String[] args) {
        IPAddressCodec ipAddressCodec = new IPAddressCodec();
        byte[] bytes = {0x0A, 0x0B, 0x0C, 0x0D, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        String decode = ipAddressCodec.decode(bytes);
        System.out.println(decode);
        byte[] encode = ipAddressCodec.encode(decode);
        System.out.println(HexUtil.encodeHexStr(encode).toUpperCase());
    }
}
