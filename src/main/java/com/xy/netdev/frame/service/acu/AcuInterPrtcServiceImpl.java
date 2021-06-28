package com.xy.netdev.frame.service.acu;


import cn.hutool.core.bean.BeanUtil;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.modemscmm.ModemScmmInterPrtcServiceImpl;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.transit.IDataReciveService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;
import static com.xy.netdev.common.util.ByteUtils.byteToNumber;
import static com.xy.netdev.frame.service.gf.GfPrtcServiceImpl.isFloat;
import static com.xy.netdev.frame.service.gf.GfPrtcServiceImpl.isUnsigned;


/**
 * ACU卫通天线参数协议解析
 *
 * @author luo
 * @date 2021-03-05
 */
@Component
public class AcuInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    ISysParamService sysParamService;

    @Autowired
    IDataReciveService dataReciveService;
    @Autowired
    private ModemScmmInterPrtcServiceImpl modemScmmInterPrtcService;

    @Override
    public void queryPara(FrameReqData reqInfo) {

    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer
                .getInterLinkParaList(respData.getDevType(), respData.getCmdMark());
        byte[] bytes = respData.getParamBytes();
        List<FrameParaData> frameParaDataList = frameParaInfos.stream()
                .filter(Objects::nonNull)
                .map(frameParaInfo -> {
                    FrameParaData paraInfo = new FrameParaData();
                    BeanUtil.copyProperties(frameParaInfo, paraInfo, true);
                    BeanUtil.copyProperties(respData, paraInfo, true);
                    paraInfo.setLen(Integer.parseInt(frameParaInfo.getParaByteLen()));
                    String val = byteToNumber(bytes, frameParaInfo.getParaStartPoint(),
                            Integer.parseInt(frameParaInfo.getParaByteLen())
                            ,isUnsigned(sysParamService, frameParaInfo.getAlertPara())
                            ,isFloat(sysParamService, frameParaInfo.getAlertPara())
                    ).toString();
                    FrameParaInfo paraInfoByNo = BaseInfoContainer.getParaInfoByNo(paraInfo.getDevType(), paraInfo.getParaNo());
                    String codec = paraInfoByNo.getNdpaRemark2Data();
                    if (StringUtils.isNotBlank(codec)){
                        Integer startPoint = frameParaInfo.getParaStartPoint();
                        int byteLen = Integer.parseInt(frameParaInfo.getParaByteLen());
                        byte[] targetBytes = byteArrayCopy(bytes, startPoint, byteLen);
                        val = modemScmmInterPrtcService.doGetValue(paraInfoByNo,targetBytes);
                    }else {
                        if(paraInfo.getParaNo().equals("18") || paraInfo.getParaNo().equals("20")){
                            if(val.length()>=4){
                                val = val.substring(0,3)+"."+val.substring(3,4);
                            }
                        }
                        if(paraInfo.getParaNo().equals("19")){
                            if(val.length()>=3){
                                val = val.substring(0,2)+"."+val.substring(2,3);
                            }
                        }
                        if(paraInfo.getParaNo().equals("8")||paraInfo.getParaNo().equals("11")){
                            if(val.length()>=5){
                                val = val.substring(0,3)+"."+val.substring(3,5);
                            }
                        }
                        if(paraInfo.getParaNo().equals("9")||paraInfo.getParaNo().equals("10")||paraInfo.getParaNo().equals("12")||paraInfo.getParaNo().equals("13")){
                            Double num = Double.parseDouble(val);
                            if(num>=0 && val.length()>=4){
                                val = val.substring(0,2)+"."+val.substring(2,4);
                            }
                            if(num<0 && val.length()>=5){
                                val = val.substring(0,3)+"."+val.substring(3,5);
                            }
                        }
                    }
                    paraInfo.setParaVal(val);
                    return paraInfo;
                }).collect(Collectors.toList());
        respData.setFrameParaList(frameParaDataList);
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }




}
