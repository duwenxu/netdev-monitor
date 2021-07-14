package com.xy.netdev.frame.service.tkuka;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.xy.common.exception.BaseException;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.common.util.SpringContextUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.ParamCodec;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.xy.netdev.common.constant.SysConfigConstant.PARA_COMPLEX_LEVEL_COMPOSE;
import static com.xy.netdev.common.constant.SysConfigConstant.PARA_DATA_TYPE_INT;
import static com.xy.netdev.container.DevLogInfoContainer.PARA_REPS_STATUS_SUCCEED;

/**
 * TKuka0.9CA监控设备
 * @author sunchao
 * @create 2021-05-31 14:00
 */
@Service
@Slf4j
public class TkukaCaPrtcServiceImpl implements IQueryInterPrtclAnalysisService {
    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReciveService dataReciveService;

    /**
     * 状态上报包帧头标识
     */
    private static final String RPT_IDS = "7b";
    private static String Flag = "T";

    private int num= 0;

    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        if(num==4){
            num = 0;
        }else{
            num ++;
            return respData;
        }
       byte[] bytes = respData.getParamBytes();
        if (ObjectUtil.isNull(bytes)) {
            log.warn("TKuka0.9CA监控设备查询响应异常, 未获取到数据体, 设备编号：[{}], 信息:[{}]", respData.getDevNo(), JSON.toJSONString(respData));
            return respData;
        }
        //响应标识 帧头
        String cmdMark = respData.getCmdMark();
        List<FrameParaData> frameParaDataList = new ArrayList<>();
        //**查询上报*//*
        if (RPT_IDS.equals(cmdMark)) {
            setFrameDataList(respData, bytes, cmdMark, frameParaDataList);
            respData.setRespCode(PARA_REPS_STATUS_SUCCEED);
            //**错误应答信息*//
        } else {
            throw new BaseException("TKuka0.9CA监控设备查询响应解析异常：非法的帧头:" + cmdMark);
        }
        respData.setFrameParaList(frameParaDataList);
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }

    private void setFrameDataList(FrameRespData respData, byte[] bytes, String cmdMark, List<FrameParaData> frameParaDataList) {
        //获取接口单元的参数信息
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getInterLinkParaList(respData.getDevType(), cmdMark);
        int startPoint = 0;  //重新计算起始字节：因为上报帧中包含无效字节
        for (FrameParaInfo param : frameParaInfos) {
            byte[] byte1 = ByteUtils.byteArrayCopy(bytes, startPoint, Integer.valueOf(param.getParaByteLen()));
            if(PARA_COMPLEX_LEVEL_COMPOSE.equals(param.getCmplexLevel())){
                String strB = ByteUtils.byteToBinary(byte1[0]).substring(4);
                String value = "";
                int startNum = 0;
                int endNum = 0;
                for(FrameParaInfo frameParaInfo : param.getSubParaList()){
                    endNum = endNum+Integer.valueOf(frameParaInfo.getParaStrLen());
                    genFramePara(frameParaInfo,respData.getDevNo(),strB.substring(startNum,endNum),frameParaDataList);
                    value = value + strB.substring(startNum,endNum)+"_";
                    startNum = endNum;
                }
                genFramePara(param,respData.getDevNo(),value,frameParaDataList);
            }else{
                if(param.getParaNo().equals("37") || param.getParaNo().equals("49")){
                    String str = HexUtil.encodeHexStr(byte1);
                    if(str.equals("30302E30")){
                        genFramePara(param,respData.getDevNo(),"0.0",frameParaDataList);
                    }else if(str.equals("203c3230")){
                        genFramePara(param,respData.getDevNo(),"<20",frameParaDataList);
                    }else{
                        genFramePara(param, respData, byte1, frameParaDataList);
                    }
                }else if(param.getParaNo().equals("14")){
                    genFramePara(param,respData.getDevNo(),"108.86",frameParaDataList);
                }else if(param.getParaNo().equals("15")){
                    genFramePara(param,respData.getDevNo(),"34.18",frameParaDataList);
                }else{
                    genFramePara(param, respData, byte1, frameParaDataList);
                }
            }
            //计算起始字节
            startPoint = startPoint+Integer.valueOf(param.getParaByteLen());
            if(StringUtils.isNotBlank(param.getNdpaRemark1Data())){
                startPoint = startPoint+Integer.valueOf(param.getNdpaRemark1Data());
            }
        }
        //设置部分参数不显示
        String strs = "60,61,62,63,64";
        for(String str : strs.split(",")){
            DevParaInfoContainer.setIsShow(respData.getDevNo(),str,false);
        }
    }

    /**
     * 生成FrameParaData类
     *
     * @param param
     * @return
     */
    private void genFramePara(FrameParaInfo param, FrameRespData respData, byte[] byte1, List<FrameParaData> frameParaDataList) {
        FrameParaData frameParaData = null;
        if (StringUtils.isNotBlank(param.getNdpaRemark2Data())) {
            ParamCodec handler = SpringContextUtils.getBean(param.getNdpaRemark2Data());
            genFramePara(param, respData.getDevNo(), String.valueOf(handler.decode(byte1, null)),frameParaDataList);
        } else if(param.getParaNo().equals("21")){
            //genFramePara(param, respData.getDevNo(), ByteUtils.,frameParaDataList);
        }else{
            genFramePara(param, respData.getDevNo(), HexUtil.encodeHexStr(byte1),frameParaDataList);
        }
    }

    /**
     * 生成FrameParaData类
     *
     * @param currentPara
     * @param devNo
     * @param paraValueStr
     * @return
     */
    private void genFramePara(FrameParaInfo currentPara, String devNo, String paraValueStr,List<FrameParaData> frameParaDataList) {
        FrameParaData frameParaData = FrameParaData.builder()
                .devType(currentPara.getDevType())
                .paraNo(currentPara.getParaNo())
                .devNo(devNo)
                .build();
        frameParaData.setParaVal(paraValueStr);
        frameParaDataList.add(frameParaData);
    }
}
