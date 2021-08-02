package com.xy.netdev.frame.service.shipAcu.codec;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.primitives.Bytes;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.service.ParamCodec;
import io.netty.buffer.Unpooled;
import org.springframework.stereotype.Component;

/**
 * 无端数值转换类
 * sunchao
 */
@Component
public class NumConversionUnsign implements ParamCodec {

    @Override
    public String decode(byte[] bytes, Object... objects) {
        //默认不做小数点保留
        double value = 1;//利用小端转换
        if (objects != null&&objects.length!=0) {
            if (!ObjectUtil.isEmpty(objects[0])){
                value = Bytes2Int_LE(bytes)/Double.valueOf(objects[0].toString());
            }
        }
        return String.format("%.2f",value);
    }

    @Override
    public byte[] encode(String value, Object... objects) {
        //默认不做小数点保留
        int resultInt = 1;
        byte[] bytes = null;
        if (objects != null&&objects.length!=0) {
            if (!ObjectUtil.isEmpty(objects[0])){
                if(value.contains(".")){
                    resultInt = new Double(Double.valueOf(value) * Double.valueOf(objects[0].toString())).intValue();
                }else{
                    resultInt = Integer.valueOf(value) * Integer.valueOf(objects[0].toString());
                }
                bytes = ByteUtils.objToBytes(resultInt, Unpooled.LITTLE_ENDIAN,Unpooled::copyInt);
            }
        }
        return bytes;
    }

    /**
     * 转换byte数组为int（小端）
     * @return
     * @note 数组长度至少为4，按小端方式转换,即传入的bytes是小端的，按这个规律组织成int
     */
    public static int Bytes2Int_LE(byte[] bytes){
        if(bytes.length == 4){
            int iRst = (bytes[0] & 0xFF);
            iRst |= (bytes[1] & 0xFF) << 8;
            iRst |= (bytes[2] & 0xFF) << 16;
            iRst |= (bytes[3] & 0xFF)<< 24;
            return iRst;
        }else if(bytes.length ==2){
            Bytes.reverse(bytes);
            return ByteUtils.byteToNumber(bytes,0,bytes.length,true,true).intValue();
        }
        return -1;
    }
}
