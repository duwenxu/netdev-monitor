package com.xy.netdev.frame.service.freqcov;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.modem.ModemInterPrtcServiceImpl;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;

/**
 * 6914变频器接口查询实现
 *
 * @author duwenxu
 * @create 2021-04-14 9:53
 */
@Service
@Slf4j
public class FreqConvertInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {
    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReciveService dataReciveService;
    @Autowired
    private ModemInterPrtcServiceImpl modemInterPrtcService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        if (ObjectUtil.isNull(bytes)) {
            log.warn("6914变频器查询响应异常, 未获取到数据体, 设备编号：[{}], 信息:[{}]", respData.getDevNo(), JSON.toJSONString(respData));
            return respData;
        }
        String cmdMark = respData.getCmdMark();
        //获取接口单元的参数信息
        List<FrameParaData> frameParaDataList = new ArrayList<>();
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getInterLinkParaList(respData.getDevType(), cmdMark);
        for (FrameParaInfo param : frameParaInfos) {
            if (Objects.nonNull(param)) {
                //构造返回信息体 paraInfo
                Integer startPoint = param.getParaStartPoint();
                int byteLen = Integer.parseInt(param.getParaByteLen());
                byte[] targetBytes = byteArrayCopy(bytes, startPoint, byteLen);

                FrameParaData paraData = modemInterPrtcService.doGetParam(respData, targetBytes, param);
                frameParaDataList.add(paraData);
            }
        }
        respData.setFrameParaList(frameParaDataList);
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }
}
