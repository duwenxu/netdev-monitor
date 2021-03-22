package com.xy.netdev.frame.service.impl.head;

import cn.hutool.core.util.StrUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.base.AbsDeviceSocketHandler;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.entity.SocketEntity;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.Objects;

import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;

/**
 * 1.2米ACU天线
 */
@Service
public class AcuImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {

    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService,
                         IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService) {
        if (iParaPrtclAnalysisService != null){
            iParaPrtclAnalysisService.ctrlParaResponse(frameRespData);
            return;
        }
        iQueryInterPrtclAnalysisService.queryParaResponse(frameRespData);
    }

    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
        byte[] bytes = socketEntity.getBytes();
        //长度为69则为主动上报. 否则为参数
        if (bytes.length == 69){
            String cmd = new String(Objects.requireNonNull(byteArrayCopy(bytes, 1, 3)));
            byte[] paramBytes = ByteUtils.byteArrayCopy(bytes, 4, 44);
            frameRespData.setParamBytes(paramBytes);
            frameRespData.setCmdMark(cmd);
            return frameRespData;
        }
        String str =  StrUtil.str(bytes, Charset.defaultCharset());
        frameRespData.setParamBytes(bytes);
        frameRespData.setCmdMark(StrUtil.sub(str, 1, 6));
        return frameRespData;
    }

    @Override
    public byte[] pack(FrameReqData frameReqData) {
        return frameReqData.getParamBytes();
    }

}
