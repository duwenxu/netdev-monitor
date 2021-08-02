package com.xy.netdev.frame.service.shortwave;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.HexUtil;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.frame.service.modemscmm.ModemScmmInterPrtcServiceImpl;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.rpt.enums.ShortWaveCmkEnum;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.impl.DataReciveServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.xy.netdev.common.constant.SysConfigConstant.PARA_COMPLEX_LEVEL_COMPOSE;
import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;
import static com.xy.netdev.monitor.constant.MonitorConstants.SUB_KU_GF;
import static com.xy.netdev.monitor.constant.MonitorConstants.SUB_MODEM;
import static com.xy.netdev.rpt.enums.ShortWaveCmkEnum.QUERY_CHANNEL;


/**
 * 750-400W短波设备(接口参数查询)
 */
@Slf4j
@Component
public class ShortWaveInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {
    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private DataReciveServiceImpl dataReciveService;
    @Autowired
    private ModemScmmInterPrtcServiceImpl modemScmmInterPrtcService;

    /**固定序号校验*/
    public static final byte[] QUERY_CHANNEL_STATUS = new byte[]{0x00,0x00,0x00,0x00};
    public static final byte[] END_CHANNEL = new byte[]{0x00,0x00,0x00,0x02};

    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        String devType = respData.getDevType();
        String devNo = respData.getDevNo();
        String cmdMark = respData.getCmdMark();
        /**命令序号*/
        byte[] serialNum = byteArrayCopy(bytes, bytes.length - 4, 4);
        byte[] paramsBytes = byteArrayCopy(bytes, 0, bytes.length - 4);

        List<FrameParaData> frameParaList = new ArrayList<>();
        /**查询信道*/
        if (QUERY_CHANNEL.getTempResCmk().equals(cmdMark)){
            if (!Arrays.equals(serialNum, QUERY_CHANNEL_STATUS)){
                log.error("400W短波设备查询信道信息应答帧序号错误：预期序号：[{}],收到序号：[{}]",HexUtil.encodeHex(QUERY_CHANNEL_STATUS),HexUtil.encodeHex(serialNum));
            }
            addTempStatus(devType, paramsBytes, frameParaList, QUERY_CHANNEL,respData);
        }else if (QUERY_CHANNEL.getRespCmk().equals(cmdMark)){
            if (!Arrays.equals(serialNum, QUERY_CHANNEL_STATUS)){
                log.error("400W短波设备查询信道信息应答帧序号错误：预期序号：[{}],收到序号：[{}]",HexUtil.encodeHex(QUERY_CHANNEL_STATUS),HexUtil.encodeHex(serialNum));
            }
            List<FrameParaInfo> paraList = BaseInfoContainer.getInterLinkParaList(devType, QUERY_CHANNEL.getRespCmk());
            for (FrameParaInfo paraInfo : paraList) {
                if (PARA_COMPLEX_LEVEL_COMPOSE.equals(paraInfo.getCmplexLevel())){
                    List<FrameParaInfo> subParaList = paraInfo.getSubParaList();
                    addParamList(devNo, paramsBytes, frameParaList, subParaList,respData);
                    addParentParam(respData, paramsBytes, frameParaList, paraInfo);
                }else {
                    addParams(devNo, paramsBytes, frameParaList, paraInfo,respData);
                }
            }
        /**拆链响应*/
        }else if (ShortWaveCmkEnum.END_CHANNEL.getTempResCmk().equals(cmdMark)){
            if (!Arrays.equals(serialNum, END_CHANNEL)){
                log.error("400W短波设备拆链应答帧序号错误：预期序号：[{}],收到序号：[{}]",HexUtil.encodeHex(END_CHANNEL),HexUtil.encodeHex(serialNum));
            }
            addTempStatus(devType, paramsBytes, frameParaList, ShortWaveCmkEnum.END_CHANNEL,respData);
        }else if(ShortWaveCmkEnum.END_CHANNEL.getRespCmk().equals(cmdMark)){
            if (!Arrays.equals(serialNum, END_CHANNEL)){
                log.error("400W短波设备拆链应答帧序号错误：预期序号：[{}],收到序号：[{}]",HexUtil.encodeHex(END_CHANNEL),HexUtil.encodeHex(serialNum));
            }
            List<FrameParaInfo> paraList = BaseInfoContainer.getInterLinkParaList(devType, ShortWaveCmkEnum.END_CHANNEL.getRespCmk());
            for (FrameParaInfo paraInfo : paraList) {
                if (PARA_COMPLEX_LEVEL_COMPOSE.equals(paraInfo.getCmplexLevel())){
                    List<FrameParaInfo> subParaList = paraInfo.getSubParaList();
                    addParamList(devNo, paramsBytes, frameParaList, subParaList,respData);
                    addParentParam(respData, paramsBytes, frameParaList, paraInfo);
                }else {
                    addParams(devNo, paramsBytes, frameParaList, paraInfo,respData);
                }
            }
        /**上报数据*/
        } else if (ShortWaveCmkEnum.RPT_STATUS.getRespCmk().equals(cmdMark)) {
            List<FrameParaInfo> paraList = BaseInfoContainer.getInterLinkParaList(devType, ShortWaveCmkEnum.RPT_STATUS.getRespCmk());
            for (FrameParaInfo paraInfo : paraList) {
                addParams(devNo, paramsBytes, frameParaList, paraInfo, respData);
            }
        }
        respData.setFrameParaList(frameParaList);
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }

    public synchronized void addParentParam(FrameRespData respData, byte[] paramsBytes, List<FrameParaData> frameParaList,FrameParaInfo paraInfo) {
        FrameParaData paraData = new FrameParaData();
        BeanUtil.copyProperties(respData, paraData, true);
        BeanUtil.copyProperties(paraInfo, paraData, true);
        //添加复杂参数  父参数
        paraData.setParaVal(HexUtil.encodeHexStr(paramsBytes));
        frameParaList.add(paraData);
    }

    /***
     * @Description 临时响应的单参数赋值
     * @Date 10:44 2021/7/13
     * @author 嗜雪的蚂蚁
     **/
    public synchronized void addTempStatus(String devType, byte[] paramsBytes, List<FrameParaData> frameParaList, ShortWaveCmkEnum startChannel,FrameRespData respData) {
        FrameParaInfo paraInfoByCmd = BaseInfoContainer.getParaInfoByCmd(devType, startChannel.getTempResCmk());
        String value = modemScmmInterPrtcService.doGetValue(paraInfoByCmd, paramsBytes);
        FrameParaData frameParaData = new FrameParaData();
        BeanUtil.copyProperties(paraInfoByCmd, frameParaData,true);
        BeanUtil.copyProperties(respData,frameParaData,true);
        frameParaData.setParaVal(value);
        frameParaList.add(frameParaData);
    }

    /***
     * @Description 接口响应的多参数赋值
     * @Date 10:45 2021/7/13
     * @author 嗜雪的蚂蚁
     **/
    public synchronized void addParams(String devNo, byte[] paramsBytes, List<FrameParaData> frameParaList, FrameParaInfo subPara, FrameRespData respData) {
            Integer startPoint = subPara.getParaStartPoint();
            String paramByteLen = subPara.getParaByteLen();
            int byteIndex = 0;
            int byteLen = 1;
            if (startPoint != null) {
                byteIndex = startPoint;
            }
            if (StringUtils.isNotBlank(paramByteLen)) {
                byteLen = Integer.parseInt(paramByteLen);
            }
            byte[] subByte = ByteUtils.byteArrayCopy(paramsBytes, byteIndex, byteLen);
            String value = modemScmmInterPrtcService.doGetValue(subPara, subByte);
            FrameParaData subParaData = new FrameParaData();
            BeanUtil.copyProperties(subPara, subParaData, true);
            BeanUtil.copyProperties(respData, subParaData, true);
            subParaData.setParaVal(value);
            subParaData.setDevNo(devNo);
            frameParaList.add(subParaData);
    }

    public synchronized void addParamList(String devNo, byte[] paramsBytes, List<FrameParaData> frameParaList, List<FrameParaInfo> subParaList, FrameRespData respData) {
        int seq = 0;
        int point = 0;
        for (FrameParaInfo subPara : subParaList) {
            seq++;
            subPara.setParaSeq(seq);  //参数序号
            subPara.setParaStartPoint(point);//参数下标：从哪一个字节开始
            log.debug("cmd:{}----point:{}", subPara.getCmdMark(), point);
            String byteLen = org.apache.commons.lang.StringUtils.isBlank(subPara.getParaByteLen()) ? "0" : subPara.getParaByteLen();
            point = point + Integer.parseInt(byteLen);

            addParams(devNo,paramsBytes,frameParaList,subPara,respData);
        }
    }
}
