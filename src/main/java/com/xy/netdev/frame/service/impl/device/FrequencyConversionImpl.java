package com.xy.netdev.frame.service.impl.device;

import cn.hutool.core.util.StrUtil;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.entity.TransportEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;

/**
 * 1:1下变频器
 * @author cc
 */
@Service
public class FrequencyConversionImpl extends AbsDeviceSocketHandler<SocketEntity, TransportEntity> {
    @Override
    public void callback(TransportEntity transportEntity) {
        iParaPrtclAnalysisService.ctrlParaResponse(transportEntity);
    }

    @Override
    public TransportEntity unpack(SocketEntity socketEntity, TransportEntity transportEntity) {
        String data = new String(socketEntity.getBytes(), Charset.defaultCharset());
        int beginOffset = StrUtil.indexOf( data, '/');
        int endOffset = StrUtil.indexOf(data, '_');
        String paramMark = StrUtil.sub(data, beginOffset, endOffset);
        transportEntity.setParamMark(paramMark);
        transportEntity.setParamBytes(socketEntity.getBytes());
        return transportEntity;
    }

    @Override
    public byte[] pack(TransportEntity transportEntity) {
        return transportEntity.getParamBytes();
    }
}
