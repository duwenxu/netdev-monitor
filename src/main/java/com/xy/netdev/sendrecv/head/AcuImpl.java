package com.xy.netdev.sendrecv.head;

import cn.hutool.core.util.StrUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.ICtrlInterPrtclAnalysisService;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.sendrecv.base.AbsDeviceSocketHandler;
import com.xy.netdev.sendrecv.entity.SocketEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;

/**
 * 1.2米ACU天线
 */
@Service
@Slf4j
public class AcuImpl extends AbsDeviceSocketHandler<SocketEntity, FrameReqData, FrameRespData> {

    @Override
    public void callback(FrameRespData frameRespData, IParaPrtclAnalysisService iParaPrtclAnalysisService,
                         IQueryInterPrtclAnalysisService iQueryInterPrtclAnalysisService, ICtrlInterPrtclAnalysisService ctrlInterPrtclAnalysisService) {
        if (iParaPrtclAnalysisService != null){
            iParaPrtclAnalysisService.ctrlParaResponse(frameRespData);
            return;
        }
        iQueryInterPrtclAnalysisService.queryParaResponse(frameRespData);
    }

    @Override
    public String cmdMarkConvert(FrameRespData frameRespData) {
        //获取设备CMD信息, '/'为调制解调器特殊格式, 因为调制解调器cmd为字符串, 不能进行十六进制转换, 所以特殊区分
        return frameRespData.getCmdMark();
    }

    @Override
    public FrameRespData unpack(SocketEntity socketEntity, FrameRespData frameRespData) {
        byte[] bytes = socketEntity.getBytes();
        //长度为69则为主动上报. 否则为参数
        if (bytes.length == 69){
            String cmd = new String(Objects.requireNonNull(byteArrayCopy(bytes, 1, 3)));
            byte[] paramBytes = ByteUtils.byteArrayCopy(bytes, 4, 53);
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
        byte[] paramBytes = frameReqData.getParamBytes();
        String comd = StrUtil.str(paramBytes, StandardCharsets.UTF_8);
        log.debug("2.4mAcu天线发送控制指令：[{}]",comd);
        return paramBytes;
    }

}
