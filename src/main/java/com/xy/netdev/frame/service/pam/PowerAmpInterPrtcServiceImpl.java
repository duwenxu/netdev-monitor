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
import com.xy.netdev.frame.service.modemscmm.ModemScmmInterPrtcServiceImpl;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.transit.IDataReciveService;
import com.xy.netdev.transit.impl.DataReciveServiceImpl;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private DataReciveServiceImpl dataReciveService;
    @Autowired
    private ModemScmmInterPrtcServiceImpl modemScmmInterPrtcService;
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
        //对于位参数记录上一个参数的bytes
        byte[] previousBytes = new byte[0];
        List<FrameParaData> frameParaDataList = new ArrayList<>(frameParaInfos.size());
        for (FrameParaInfo param : frameParaInfos) {
            if (Objects.nonNull(param)) {
                //构造返回信息体 paraInfo
                FrameParaData paraInfo = new FrameParaData();
                BeanUtil.copyProperties(param, paraInfo, true);
                BeanUtil.copyProperties(respData, paraInfo, true);
                Integer startPoint = param.getParaStartPoint();
                String byteLen = param.getParaByteLen();

                //字节长度位空或者0时，直接取上一次的字节
                int paraByteLen = 0;
                byte[] targetBytes;
                String value = null;
                try {
                    if (StringUtils.isNotBlank(byteLen)) {
                        paraByteLen = Integer.parseInt(byteLen);
                        paraInfo.setLen(paraByteLen);
                        //获取参数字节
                        targetBytes = byteArrayCopy(bytes, startPoint, paraByteLen);
                        previousBytes = targetBytes;
                    } else {
                        targetBytes = previousBytes;
                    }
                    value = modemScmmInterPrtcService.doGetValue(param, paraInfo, targetBytes);
                    log.debug("东森功放 参数编号：{}，参数字节：{}，参数值：{}", paraInfo.getParaNo(), HexUtil.encodeHexStr(targetBytes), value);
                } catch (Exception e) {
                    log.error("参数编号：[{}]字节长度截取错误，起始位置：{}，字节长度：{}", param.getParaNo(), startPoint, paraByteLen);
                }
                paraInfo.setParaVal(value);
                frameParaDataList.add(paraInfo);
            }
        }
        //接口参数查询响应固定为 查询成功
        respData.setRespCode("0");
        respData.setFrameParaList(frameParaDataList);
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }
}