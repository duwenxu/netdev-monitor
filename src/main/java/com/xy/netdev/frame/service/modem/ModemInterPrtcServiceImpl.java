package com.xy.netdev.frame.service.modem;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.util.BeanFactoryUtil;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;

/**
 * 650调制解调器接口查询实现
 *
 * @author duwenxu
 * @create 2021-04-09 9:36
 */
@Service
@Slf4j
public class ModemInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    ISysParamService sysParamService;
    @Autowired
    private IDataReciveService dataReciveService;
    /**查询应答帧 分隔符*/
    private static final String SPLIT = "5f";

    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        if (ObjectUtil.isNull(bytes)) {
            log.warn("650调制解调器查询响应异常, 未获取到数据体, 信息:{}", JSON.toJSONString(respData));
            return respData;
        }
        //获取接口单元的参数信息
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getInterLinkParaList(respData.getDevType(), respData.getCmdMark());
        //根据关键字获取参数
        List<FrameParaData> frameParaDataList = new ArrayList<>();
        for (FrameParaInfo param :  frameParaInfos) {
            if (Objects.nonNull(param)){
                //构造返回信息体 paraInfo
                Integer startPoint = param.getParaStartPoint();
                String byteLen = param.getParaByteLen();

                //字节长度位空或者0时，直接取上一次的字节
                int paraByteLen;
                byte[] targetBytes = new byte[]{};
                if (StringUtils.isNotBlank(byteLen)) {
                    paraByteLen = Integer.parseInt(byteLen);
                    //获取参数字节
                    try {
                        targetBytes = byteArrayCopy(bytes, startPoint, paraByteLen);
                    } catch (Exception e) {
                        log.error("参数编号：[{}]字节长度截取错误，起始位置：{}，字节长度：{}", param.getParaNo(),startPoint,paraByteLen);
                    }
                }
                //获取单个参数的解析结果
                FrameParaData paraData = doGetParam(respData, targetBytes, param);
                frameParaDataList.add(paraData);
            }
        }
        respData.setFrameParaList(frameParaDataList);
        //参数查询响应结果接收
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }

    /**
     * 解析获取指定Bytes的参数值
     * @param respData  响应数据结构
     * @param paraValBytes 字节数组
     * @param currentPara 当前参数信息体
     * @return  解析出的参数体结构
     */
    public FrameParaData doGetParam(FrameRespData respData, byte[] paraValBytes, FrameParaInfo currentPara) {
        if (StringUtils.isEmpty(currentPara.getParaNo())){
            return null;
        }
        FrameParaData paraInfo = new FrameParaData();
        BeanUtil.copyProperties(currentPara, paraInfo, true);
        BeanUtil.copyProperties(respData, paraInfo, true);

        //获取参数解析配置信息
        String confClass = currentPara.getNdpaRemark2Data();
        String confParams = currentPara.getNdpaRemark3Data();
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
            value = codec.decode(paraValBytes, params);
        } catch (Exception e) {
            log.error("参数解析异常：{}",paraInfo);
        }
        paraInfo.setParaVal(value);
        return paraInfo;
    }
}
