package com.xy.netdev.frame.service.shipAcu;

import cn.hutool.core.util.ArrayUtil;
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
 * 1.5米ACU天线查询实现(船载)
 *
 * @author sunchao
 * @create 2021-05-19 11:08
 */
@Service
@Slf4j
public class ShipAcuPrtcServiceImpl implements IQueryInterPrtclAnalysisService {
    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReciveService dataReciveService;

    /**
     * 状态上报包帧头标识
     */
    private static final String RPT_IDS = "7c";
    private static String Flag = "T";

    @Override
    public void queryPara(FrameReqData reqInfo) {
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        byte[] bytes = respData.getParamBytes();
        if (ObjectUtil.isNull(bytes)) {
            log.warn("1.5米ACU查询响应异常, 未获取到数据体, 设备编号：[{}], 信息:[{}]", respData.getDevNo(), JSON.toJSONString(respData));
            return respData;
        }
        //响应标识 帧头
        String cmdMark = respData.getCmdMark();
        List<FrameParaData> frameParaDataList = new ArrayList<>();
        /**查询上报*/
        if (RPT_IDS.equals(cmdMark)) {
            setFrameDataList(respData, bytes, cmdMark, frameParaDataList);
            respData.setRespCode(PARA_REPS_STATUS_SUCCEED);
            /**错误应答信息*/
        } else {
            throw new BaseException("1.5米ACU查询响应解析异常：非法的帧头:" + cmdMark);
        }
        respData.setFrameParaList(frameParaDataList);
        dataReciveService.interfaceQueryRecive(respData);
        return respData;
    }

    private void setFrameDataList(FrameRespData respData, byte[] bytes, String cmdMark, List<FrameParaData> frameParaDataList) {
        //获取接口单元的参数信息
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getInterLinkParaList(respData.getDevType(), cmdMark);
        for (FrameParaInfo param : frameParaInfos) {
            byte[] byte1 = ByteUtils.byteArrayCopy(bytes, param.getParaStartPoint(), Integer.valueOf(param.getParaByteLen()));
            genFramePara(param, respData, byte1, frameParaDataList);
            //特定字解析
            if (param.getParaNo().equals("15")) {
                List<FrameParaInfo> list = BaseInfoContainer.getParasByDevType(respData.getDevType())
                        .stream().filter(frameParaInfo -> frameParaInfo.getNdpaRemark3Data().contains(Flag))
                        .collect(Collectors.toList());
                //解析特定字及后续参数
                byte[] byteEnd = ByteUtils.byteArrayCopy(bytes, 33, 12);
                int paraStartPoint = 0;
                //排序
                list.sort(Comparator.comparing(frameParaInfo -> Integer.valueOf(frameParaInfo.getParaNo())));
                for (FrameParaInfo frameParaData1 : list) {
                    byte[] byteNext = ByteUtils.byteArrayCopy(byteEnd, paraStartPoint, Integer.valueOf(frameParaData1.getParaByteLen()));
                    genFramePara(frameParaData1, respData, byteNext, frameParaDataList);
                    paraStartPoint = paraStartPoint + Integer.valueOf(frameParaData1.getParaByteLen());
                    //DevParaInfoContainer.setIsShow(respData.getDevNo(), frameParaData1.getParaNo(), true);
                }
                /*List<FrameParaInfo> listNoShow = BaseInfoContainer.getParasByDevType(respData.getDevType())
                        .stream().filter(frameParaInfo -> StringUtils.isNotBlank(frameParaInfo.getNdpaRemark3Data())
                                && !Flag.equals(frameParaInfo.getNdpaRemark3Data())).collect(Collectors.toList());
                listNoShow.forEach(frameParaInfo -> {
                    DevParaInfoContainer.setIsShow(respData.getDevNo(), frameParaInfo.getParaNo(), false);
                });*/
            }
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
        if (PARA_COMPLEX_LEVEL_COMPOSE.equals(param.getCmplexLevel())) {
            String value = "";
            //比特位分解
            String paraValueStr = ByteUtils.byteToBinary(byte1[00]);
            int paraStartPoint = 0;
            int paraEndPoint = 0;
            List<FrameParaInfo> list = param.getSubParaList();
            list.sort(Comparator.comparing(paraInfo -> Integer.valueOf(paraInfo.getParaNo())));
            for (int i = 1; i <= list.size(); i++) {
                FrameParaInfo frameParaInfo = list.get(i-1);
                String  paraVal = "";
                if(list.size() == 1){
                    paraEndPoint = 4 + Integer.valueOf(frameParaInfo.getParaStrLen());
                    paraVal  =  paraValueStr.substring(4, paraEndPoint);
                }else{
                    paraEndPoint = paraStartPoint + Integer.valueOf(frameParaInfo.getParaStrLen());
                    paraVal  =  paraValueStr.substring(paraStartPoint, paraEndPoint);
                }
                if("79".equals(frameParaInfo.getParaNo())){
                    paraVal = BitToHexStr(paraVal);
                }
                FrameParaData subFrame = genFramePara(frameParaInfo, respData.getDevNo(), paraVal);
                frameParaDataList.add(subFrame);
                paraStartPoint = paraEndPoint;
                if(!"78".equals(frameParaInfo.getParaNo()) && !frameParaInfo.getNdpaRemark3Data().contains("false")){
                    value = value+paraVal + "_";
                }
            }
            frameParaData = genFramePara(param, respData.getDevNo(), value.substring(0,value.length()-1));
        } else if (StringUtils.isNotBlank(param.getNdpaRemark1Data())) {
            ParamCodec handler = SpringContextUtils.getBean(param.getNdpaRemark1Data());
            if (PARA_DATA_TYPE_INT.equals(param.getDataType())) {
                frameParaData = genFramePara(param, respData.getDevNo(), String.valueOf(handler.decode(byte1, param.getNdpaRemark2Data())));
            } else {
                frameParaData = genFramePara(param, respData.getDevNo(), String.valueOf(handler.decode(byte1, null)));
            }
        } else {
            frameParaData = genFramePara(param, respData.getDevNo(), HexUtil.encodeHexStr(byte1));
        }
        //特定字解析
        if (param.getParaNo().equals("15")) {
            Flag = frameParaData.getParaVal();
        }
        if(param.getNdpaRemark3Data().contains("false")){
            DevParaInfoContainer.setIsShow(respData.getDevNo(), param.getParaNo(), false);
        }
        frameParaDataList.add(frameParaData);
    }

    /**
     * 生成FrameParaData类
     *
     * @param currentPara
     * @param devNo
     * @param paraValueStr
     * @return
     */
    private FrameParaData genFramePara(FrameParaInfo currentPara, String devNo, String paraValueStr) {
        FrameParaData frameParaData = FrameParaData.builder()
                .devType(currentPara.getDevType())
                .paraNo(currentPara.getParaNo())
                .devNo(devNo)
                .build();
        frameParaData.setParaVal(paraValueStr);
        return frameParaData;
    }

    /**
     * Bit转Byte
     */
    private String BitToHexStr(String byteStr) {
        byteStr = StringUtils.leftPad(byteStr,8-byteStr.length(),"0");
        int re ;
        if (byteStr.charAt(0) == '0') {// 正数
            re = Integer.parseInt(byteStr, 2);
        } else {// 负数
            re = Integer.parseInt(byteStr, 2) - 256;
        }
        return HexUtil.encodeHexStr(new byte[]{(byte) re}) ;
    }
}
