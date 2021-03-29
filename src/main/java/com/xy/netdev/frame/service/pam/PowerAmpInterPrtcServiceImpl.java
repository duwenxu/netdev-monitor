package com.xy.netdev.frame.service.pam;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.transit.IDataReciveService;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xy.netdev.common.util.ByteUtils.*;
import static com.xy.netdev.frame.service.gf.GfPrtcServiceImpl.isUnsigned;
import static com.xy.netdev.monitor.constant.MonitorConstants.*;

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
    @Autowired
    private IDataReciveService dataReciveService;
    private static final String QUERY = "82";

    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer
                .getInterLinkParaList(respData.getDevType(), QUERY);
        byte[] bytes = respData.getParamBytes();
        if (ObjectUtil.isNull(bytes)) {
            log.warn("400W功放查询响应异常, 未获取到数据体, 信息:{}", JSON.toJSONString(respData));
            return respData;
        }
        List<FrameParaData> frameParaDataList = frameParaInfos.stream()
                .filter(Objects::nonNull)
                .map(frameParaInfo -> {
                    FrameParaData paraInfo = new FrameParaData();
                    BeanUtil.copyProperties(frameParaInfo, paraInfo, true);
                    BeanUtil.copyProperties(respData, paraInfo, true);
                    String byteLen = frameParaInfo.getParaByteLen();
                    int paraByteLen = 0;
                    if (StringUtils.isNotBlank(byteLen)) {
                        paraByteLen = Integer.parseInt(byteLen);
                        paraInfo.setLen(paraByteLen);
                    }
                    String dataType = frameParaInfo.getDataType();
                    Integer paraStartPoint = frameParaInfo.getParaStartPoint();
                    String paraNo = paraInfo.getParaNo();
                    if (dataType.equals(STR)) {
                        byte[] targetBytes = byteArrayCopy(bytes, paraStartPoint, paraByteLen);
                        String paraVal = StrUtil.str(targetBytes, Charsets.UTF_8);
                        paraInfo.setParaVal(paraVal);
                    } else if (dataType.equals(INT)) {
                        byte[] targetBytes = byteArrayCopy(bytes, paraStartPoint, paraByteLen);
                        String paraVal = byteToNumber(bytes, paraStartPoint, paraByteLen, isUnsigned(sysParamService, INT)).toString();
                        if ("7".equals(paraNo)) {
                            paraInfo.setParaVal(String.valueOf(HexUtil.encodeHex(targetBytes)));
                        }else if ("8".equals(paraNo)) {
                            double i = (double) Integer.parseInt(paraVal) / 1000;
                            paraInfo.setParaVal("V"+i);
                        }else {
                            paraInfo.setParaVal(paraVal);
                        }
                    } else if (dataType.equals(BYTE)) {
                        Byte statusByte = bytesToNum(bytes, 21, 1, ByteBuf::readByte);
                        //分别对应第 7，6，5位
                        if ("9".equals(paraNo)) {
                            paraInfo.setParaVal(String.valueOf(statusByte & 1));
                        } else if ("10".equals(paraNo)) {
                            paraInfo.setParaVal(String.valueOf(statusByte & 8));
                        } else if ("11".equals(paraNo)) {
                            paraInfo.setParaVal(String.valueOf(statusByte & 64));
                        }
                    }
                    return paraInfo;
                }).collect(Collectors.toList());
        respData.setFrameParaList(frameParaDataList);
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }
}