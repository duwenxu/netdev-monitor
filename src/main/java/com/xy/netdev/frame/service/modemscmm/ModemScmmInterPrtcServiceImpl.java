package com.xy.netdev.frame.service.modemscmm;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.xy.netdev.common.util.BeanFactoryUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.factory.SingletonFactory;
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
import com.xy.netdev.transit.impl.DataReciveServiceImpl;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private DataReciveServiceImpl dataReciveService;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        //按单元查询参数
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        log.debug("接收到查询响应帧内容:[{}]", HexUtil.encodeHexStr(bytes));
        if (ObjectUtil.isNull(bytes)) {
            log.warn("2300调制解调器查询响应异常, 未获取到数据体, 信息:{}", JSON.toJSONString(respData));
            return respData;
        }
        //单元信息
        Short unit = bytesToNum(bytes, 0, 1, ByteBuf::readUnsignedByte);
        String hexUnit = lefPadNumToHexStr(unit);
        byte[] realBytes = new byte[bytes.length-1];
        System.arraycopy(bytes,1,realBytes,0,bytes.length-1);
        //获取接口单元的参数信息
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getInterLinkParaList(respData.getDevType(), hexUnit);
        //对于位参数记录上一个参数的bytes
        byte[] previousBytes = new byte[0];
        List<FrameParaData> frameParaDataList = new ArrayList<>(frameParaInfos.size());
        for (FrameParaInfo param : frameParaInfos) {
            if (Objects.nonNull(param)){
                //构造返回信息体 paraInfo
                FrameParaData paraInfo = new FrameParaData();
                BeanUtil.copyProperties(param, paraInfo, true);
                BeanUtil.copyProperties(respData, paraInfo, true);
                Integer startPoint = param.getParaStartPoint();
                String byteLen = param.getParaByteLen();

                //字节长度位空或者0时，直接取上一次的字节
                int paraByteLen;
                byte[] targetBytes;
                if (StringUtils.isNotBlank(byteLen)) {
                    paraByteLen = Integer.parseInt(byteLen);
                    paraInfo.setLen(paraByteLen);
                    //获取参数字节
                    targetBytes = byteArrayCopy(realBytes, startPoint, paraByteLen);
                    previousBytes = targetBytes;
                }else {
                    targetBytes = previousBytes;
                }
                String value = doGetValue(param, targetBytes);

                paraInfo.setParaVal(value);
                paraInfo.setParaOrigByte(targetBytes);
                //特殊处理 发载波电平
                if (paraInfo.getParaNo().equals("2")){
                    paraInfo.setParaVal("-"+ paraInfo.getParaVal());
                }
                frameParaDataList.add(paraInfo);
            }
        }
        //接口参数查询响应固定为 查询成功
        respData.setRespCode("0");
        respData.setFrameParaList(frameParaDataList);
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }

    /**
     * 2300 获取参数值 对应存在 bit类型数据，需要复用字节的设备协议
     * @param param 参数
     * @param targetBytes 参数字节
     * @return 参数值
     */
    public String doGetValue(FrameParaInfo param, byte[] targetBytes) {
        //获取参数解析配置信息
        String confClass = param.getNdpaRemark2Data();
        String confParams = param.getNdpaRemark3Data();
        //默认直接转换
        ParamCodec codec = SingletonFactory.getInstance(DirectParamCodec.class);
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
            log.error("参数解析异常：{}",param);
        }
        return value;
    }

}
