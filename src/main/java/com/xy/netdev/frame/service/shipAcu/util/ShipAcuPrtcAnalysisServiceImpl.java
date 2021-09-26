package com.xy.netdev.frame.service.shipAcu.util;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.xy.common.exception.BaseException;
import com.xy.netdev.common.util.ByteUtils;
import com.xy.netdev.common.util.SpringContextUtils;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.ParamCodec;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.xy.netdev.common.constant.SysConfigConstant.*;
import static com.xy.netdev.container.DevLogInfoContainer.PARA_REPS_STATUS_SUCCEED;

/**
 * 1.5米ACU天线查询实现(船载)
 *
 * @author sunchao
 * @create 2021-05-19 11:08
 */
@Service
@Slf4j
public class ShipAcuPrtcAnalysisServiceImpl implements IQueryInterPrtclAnalysisService {
    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    private IDataReceiveService dataReciveService;

    private int num =0;

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
        if(num == 5){
            num++;
            return null;
        }
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

    /**
     * 生成参数列表
     * @param respData
     * @param bytes
     * @param cmdMark
     * @param frameParaDataList
     */
    private void setFrameDataList(FrameRespData respData, byte[] bytes, String cmdMark, List<FrameParaData> frameParaDataList) {
        //获取接口单元的参数信息
        List<FrameParaInfo> frameParaInfos = BaseInfoContainer.getInterLinkParaList(respData.getDevType(), cmdMark);
        for (FrameParaInfo param : frameParaInfos) {
            byte[] byte1 = ByteUtils.byteArrayCopy(bytes, param.getParaStartPoint(), Integer.valueOf(param.getParaByteLen()));
            genFramePara(param, respData, byte1, frameParaDataList);
            //特定字解析
            if (param.getParaNo().equals("15")) {
                //过滤出包含特定字的参数：特定字的下属参数
                List<FrameParaInfo> list = BaseInfoContainer.getParasByDevType(respData.getDevType())
                        .stream().filter(frameParaInfo -> Flag.equals(frameParaInfo.getNdpaRemark3Data()))
                        .collect(Collectors.toList());
                /**---------------------------解析特定字下属参数-----------------------**/
                //获取特定字下属参数的字节
                byte[] byteEnd = ByteUtils.byteArrayCopy(bytes, 33, 12);
                int paraStartPoint = 0;
                //排序
                list.sort(Comparator.comparing(frameParaInfo -> Integer.valueOf(frameParaInfo.getParaNo())));
                //解析特定字下属参数
                for (FrameParaInfo frameParaData1 : list) {
                    byte[] byteNext = ByteUtils.byteArrayCopy(byteEnd, paraStartPoint, Integer.valueOf(frameParaData1.getParaByteLen()));
                    genFramePara(frameParaData1, respData, byteNext, frameParaDataList);
                    paraStartPoint = paraStartPoint + Integer.valueOf(frameParaData1.getParaByteLen());
                }
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
            //比特位分解
            int paraStartPoint = 0;  //参数下标
            List<FrameParaInfo> list = param.getSubParaList();
            //排序
            list.sort(Comparator.comparing(paraInfo -> Integer.valueOf(paraInfo.getParaNo())));
            String value = "";
            for (int i = 1; i <= list.size(); i++) {
                FrameParaData subFrame = null;
                FrameParaInfo frameParaInfo = list.get(i - 1);
                String paraVal = "";
                if(PARA_DATA_TYPE_INT.equals(frameParaInfo.getDataType())){
                    paraVal = ByteUtils.byteToInt(ByteUtils.byteArrayCopy(byte1, paraStartPoint, Integer.valueOf(frameParaInfo.getParaByteLen())))+"";
                    //生成参数帧
                    subFrame = genFramePara(frameParaInfo, respData.getDevNo(), paraVal);
                    //计算新的参数的字节起点
                    paraStartPoint = paraStartPoint + Integer.valueOf(frameParaInfo.getParaByteLen());
                }else if(PARA_DATA_TYPE_BYTE.equals(frameParaInfo.getDataType())){
                    subFrame = genFramePara(frameParaInfo, respData.getDevNo(), ByteUtils.byteArrayCopy(byte1,paraStartPoint,Integer.valueOf(frameParaInfo.getParaByteLen())));
                    paraVal = subFrame.getParaVal();
                    paraStartPoint = paraStartPoint + Integer.valueOf(frameParaInfo.getParaByteLen());
                }else{
                    paraVal = ByteUtils.byteToBinary(byte1[0]);
                    if(byte1.length>1){
                        paraVal = paraVal +ByteUtils.byteToBinary(byte1[1]);
                    }
                    int paraEndPoint = paraStartPoint + Integer.valueOf(frameParaInfo.getParaStrLen());
                    subFrame = genFramePara(frameParaInfo, respData.getDevNo(), paraVal.substring(paraStartPoint,paraEndPoint));
                    paraStartPoint = paraEndPoint;
                }
                frameParaDataList.add(subFrame);
                /*if("62".equals(frameParaInfo.getParaNo())){
                    DevParaInfoContainer.updateParaValue(respData.getDevNo(), ParaHandlerUtil.genLinkKey(respData.getDevNo(),"2"),paraVal);
                }else if("63".equals(frameParaInfo.getParaNo())){
                    DevParaInfoContainer.updateParaValue(respData.getDevNo(), ParaHandlerUtil.genLinkKey(respData.getDevNo(),"4"),paraVal);
                }else if("64".equals(frameParaInfo.getParaNo())){
                    DevParaInfoContainer.updateParaValue(respData.getDevNo(), ParaHandlerUtil.genLinkKey(respData.getDevNo(),"8"),paraVal);
                }*/
                value = value+paraVal+"_";
            }
            //父参数设置值
            frameParaData = genFramePara(param, respData.getDevNo(),value.substring(0,value.length()-1));
        }else {
            frameParaData = genFramePara(param, respData.getDevNo(), byte1);
        }
        //特定字解析
        if (param.getParaNo().equals("15")) {
            Flag = frameParaData.getParaVal();
        }
        if(frameParaData != null){
            frameParaDataList.add(frameParaData);
        }
    }

    /**
     * 生成FrameParaData类
     *
     * @param currentPara
     * @param devNo
     * @param bytes
     * @return
     */
    private FrameParaData genFramePara(FrameParaInfo currentPara, String devNo, Object bytes) {
        String paraValueStr = "";
        if(bytes instanceof String){
            paraValueStr = bytes.toString();
        } else if (StringUtils.isNotBlank(currentPara.getNdpaRemark2Data())) {
            //当参数的备注2包含数据则生成特殊的处理类处理
            ParamCodec handler = SpringContextUtils.getBean(currentPara.getNdpaRemark2Data());
            if (StringUtils.isNotBlank(currentPara.getNdpaRemark1Data())) {
                paraValueStr = String.valueOf(handler.decode((byte[]) bytes, currentPara.getNdpaRemark1Data()));
            } else {
                paraValueStr = String.valueOf(handler.decode((byte[]) bytes, null));
            }
        } else {
            paraValueStr = ByteUtils.byteToBinary(((byte[]) bytes)[0]).substring(4);
        }
        //生成参数帧
        FrameParaData frameParaData = FrameParaData.builder()
                .devType(currentPara.getDevType())
                .paraNo(currentPara.getParaNo())
                .devNo(devNo)
                .build();
        frameParaData.setParaVal(paraValueStr);
        return frameParaData;
    }
}
