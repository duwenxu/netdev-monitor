package com.xy.netdev.frame.service.modemscmm;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.ExtParamConf;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.ParamCodec;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.codec.DirectParamCodec;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
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

/**
 * SCMM-2300调制解调器 接口协议内容解析
 *
 * @author duwenxu
 * @create 2021-03-30 14:30
 */
@Service
@Slf4j
public class ModemScmmInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {
    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReciveService dataReciveService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        //按单元查询参数
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        if (ObjectUtil.isNull(bytes)) {
            log.warn("400W功放查询响应异常, 未获取到数据体, 信息:{}", JSON.toJSONString(respData));
            return respData;
        }
        //单元信息
        Short unit = bytesToNum(bytes, 0, 1, ByteBuf::readUnsignedByte);
        String hexUnit = lefPadNumToHexStr(unit);
        byte[] realBytes = new byte[bytes.length-1];
        System.arraycopy(bytes,1,realBytes,0,bytes.length-1);
        //获取接口单元的参数信息
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getInterLinkParaList(respData.getDevType(), hexUnit);
        List<FrameParaData> frameParaDataList = frameParaInfos.stream().filter(Objects::nonNull)
                .map(param -> {
                    //构造返回信息体 paraInfo
                    FrameParaData paraInfo = new FrameParaData();
                    BeanUtil.copyProperties(param, paraInfo, true);
                    BeanUtil.copyProperties(respData, paraInfo, true);
                    Integer startPoint = param.getParaStartPoint();
                    String byteLen = param.getParaByteLen();

                    //同一字节中不同位处理取同一个字节
                    int paraByteLen;
                    if (StringUtils.isNotBlank(byteLen)) {
                        paraByteLen = Integer.parseInt(byteLen);
                        paraInfo.setLen(paraByteLen);
                    }else {
                        paraByteLen = 1;
                    }
                    //获取参数字节
                    byte[] targetBytes = byteArrayCopy(realBytes, startPoint, paraByteLen);
                    //获取参数解析配置信息
                    String confClass = param.getNdpaRemark2Data();
                    String confParams = param.getNdpaRemark3Data();
                    //默认直接转换
                    ParamCodec codec = new DirectParamCodec();
                    ExtParamConf paramConf = new ExtParamConf();
                    Object[] params = new Object[0];
                    if (!StringUtils.isBlank(confParams)) {
                        paramConf = JSON.parseObject(confParams, ExtParamConf.class);
                    }
                    //按配置的解析方式解析
                    if (!StringUtils.isBlank(confClass)) {
                        codec = BeanFactoryUtil.getBean(confClass);
                    }
                    //构造参数
                    if (paramConf.getPoint() != null && paramConf.getStart() != null) {
                        params = new Integer[]{paramConf.getStart(), paramConf.getPoint()};
                    } else if (paramConf.getExt() != null){
                        params =paramConf.getExt().toArray();
                    }
                    String value = null;
                    try {
                        value = codec.decode(targetBytes, params);
                    } catch (Exception e) {
                        log.error("参数解析异常：{}",paraInfo);
                    }
                    paraInfo.setParaVal(value);
                    return paraInfo;
                }).collect(Collectors.toList());
        //接口参数查询响应固定为 查询成功
        respData.setRespCode("0");
        respData.setFrameParaList(frameParaDataList);
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }

    /**
     * 获取byte中指定bit的字符串
     *
     * @param byt   字节
     * @param start 起始位置
     * @param range 长度范围
     * @return bit字符串
     */
    public String bitStrByPoint(byte byt, int start, int range) {
        if (start > 7 || range > 8) {
            log.warn("输入bit范围错误：起始位置:{}.长度：{}", start, range);
        }
        return byteToBinary(byt).substring(start, start + range);
    }
}
