package com.xy.netdev.frame.service.modem;

import cn.hutool.core.bean.BeanUtil;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xy.netdev.common.util.ByteUtils.byteToNumber;

/**
 * 650型号 调制解调器
 *
 * @author duwenxu
 * @create 2021-03-11 14:23
 */
@Slf4j
@Component
public class ModemPrtcServiceImpl implements IParaPrtclAnalysisService {

    /**
     * 应答帧 分隔符
     */
    private static final String SPLIT = "5F";

    /**
     * 控制应答结果帧数据
     */
    private static final String CONTROL_SUCCESS = "20";
    private static final String CONTROL_FAIL = "21";

    @Autowired
    private SocketMutualService socketMutualService;

    @Autowired
    private IDataReciveService dataReciveService;

    @Autowired
    ISysParamService sysParamService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer
                .getInterLinkParaList(respData.getDevType(), respData.getCmdMark());
        byte[] bytes = respData.getParamBytes();
        List<FrameParaData> frameParaDataList = Objects.requireNonNull(frameParaInfos).stream()
                .map(frameParaInfo -> {
                    FrameParaData paraInfo = new FrameParaData();
                    BeanUtil.copyProperties(frameParaInfo, paraInfo, true);
                    //todo 此处参数帧之前存在分隔符，此处的方法可能不适用
                    paraInfo.setParaVal(byteToNumber(bytes, frameParaInfo.getParaStartPoint(),
                            Integer.parseInt(frameParaInfo.getParaByteLen()), isUnsigned(sysParamService, frameParaInfo.getParaNo())).toString());
                    return paraInfo;
                }).collect(Collectors.toList());
        respData.setFrameParaList(frameParaDataList);
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }

    @Override
    public void ctrlPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.CONTROL);
    }

    @Override
    public FrameRespData ctrlParaResponse(FrameRespData respData) {
        return this.queryParaResponse(respData);
    }

    public static boolean isUnsigned(ISysParamService sysParamService, String paraNo) {
        String isUnsigned = sysParamService.getParaRemark1(paraNo);
        return Integer.parseInt(isUnsigned) == 1;
    }
}
