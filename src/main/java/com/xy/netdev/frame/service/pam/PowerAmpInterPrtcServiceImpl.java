package com.xy.netdev.frame.service.pam;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.entity.PrtclFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xy.netdev.common.util.ByteUtils.byteToNumber;
import static com.xy.netdev.frame.service.gf.GfPrtcServiceImpl.isUnsigned;

/**
 * Ku400w功放 接口查询响应 帧协议解析层
 *
 * @author duwenxu
 * @create 2021-03-22 15:30
 */
@Service
@Slf4j
public class PowerAmpInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private ISysParamService sysParamService;
    private static final String QUERY = "82";

    @Override
    public void queryPara(FrameReqData reqInfo) {
        //获取查询关键字
//        PrtclFormat prtclFormat = BaseInfoContainer.getPrtclByInterfaceOrPara(reqInfo.getDevType(), reqInfo.getCmdMark());
//        String fmtSkey = prtclFormat.getFmtSkey();
//        byte[] bytes = ByteUtils.objToBytes(fmtSkey, 1);
//        reqInfo.setParamBytes(bytes);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer
                .getInterLinkParaList(respData.getDevType(), QUERY);
        byte[] bytes = respData.getParamBytes();
        if (ObjectUtil.isNull(bytes)){
            log.warn("400W功放查询响应异常, 未获取到数据体, 信息:{}", JSON.toJSONString(respData));
            return respData;
        }
        List<FrameParaData> frameParaDataList = frameParaInfos.stream()
                .filter(Objects::nonNull)
                .map(frameParaInfo -> {
                    FrameParaData paraInfo = new FrameParaData();
                    BeanUtil.copyProperties(frameParaInfo, paraInfo, true);
                    BeanUtil.copyProperties(respData, paraInfo, true);
                    paraInfo.setLen(Integer.parseInt(frameParaInfo.getParaByteLen()));
                    paraInfo.setParaVal(byteToNumber(bytes, frameParaInfo.getParaStartPoint() - 1,
                            Integer.parseInt(frameParaInfo.getParaByteLen()), isUnsigned(sysParamService,
                                    frameParaInfo.getAlertPara())).toString());
                    return paraInfo;
                }).collect(Collectors.toList());
        respData.setFrameParaList(frameParaDataList);
        return respData;
    }
}