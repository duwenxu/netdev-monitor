package com.xy.netdev.frame.service.dzt;


import cn.hutool.core.util.HexUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameReqData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.frame.service.IQueryInterPrtclAnalysisService;
import com.xy.netdev.frame.service.SocketMutualService;
import com.xy.netdev.monitor.bo.FrameParaInfo;
import com.xy.netdev.monitor.constant.MonitorConstants;
import com.xy.netdev.sendrecv.enums.ProtocolRequestEnum;
import com.xy.netdev.transit.IDataReciveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.byteToNumber;
import static com.xy.netdev.frame.service.gf.GfPrtcServiceImpl.isFloat;
import static com.xy.netdev.frame.service.gf.GfPrtcServiceImpl.isUnsigned;

/**
 * 动中通--查询接口协议解析
 * @author luo
 * @date 2021/3/26
 */

@Service
@Slf4j
public class DztQueryInterPrtcServiceImpl implements IQueryInterPrtclAnalysisService {

    @Autowired
    private SocketMutualService socketMutualService;
    @Autowired
    ISysParamService sysParamService;
    @Autowired
    private IDataReciveService dataReciveService;

    /** 全查询响应命令字*/
    public static final String QUERY_ALL_MARK = "83";
    /** 单查询响应命令字*/
    public static final String QUERY_SINGLE_MARK = "85";
    /**查询应答帧分隔符*/
    private static final String SPLIT = "2c";


    @Override
    public void queryPara(FrameReqData reqInfo) {
        //暂时是单个参数查询 cmdMark为单个参数的命令标识
        byte[] bytes = HexUtil.decodeHex(reqInfo.getCmdMark());
        reqInfo.setParamBytes(bytes);
        socketMutualService.request(reqInfo, ProtocolRequestEnum.QUERY);
    }

    @Override
    public FrameRespData queryParaResponse(FrameRespData respData) {
        String devType = respData.getDevType();
        String bytesData = HexUtil.encodeHexStr(respData.getParamBytes());
        String[] dataList = bytesData.toLowerCase().split(SPLIT.toLowerCase());
        String cmdMark = respData.getCmdMark();
        List<FrameParaData> frameParaDataList = new ArrayList<>();
        //拆分后根据关键字获取参数
        if(cmdMark.equals(QUERY_ALL_MARK)){
            for (String data : dataList) {
                String paraCmk = data.substring(0, 1);
                String paraValueStr = data.substring(1);
                List<FrameParaInfo> paraInfos =  BaseInfoContainer.getInterLinkParaList(devType, paraCmk);
                frameParaDataList.addAll(genFrameParaInfo(respData,paraInfos,paraValueStr));
            }
        }else{
            List<FrameParaInfo> paraInfos =  BaseInfoContainer.getInterLinkParaList(devType, QUERY_SINGLE_MARK);
            JSONArray array = new JSONArray();
            for (String data : dataList) {
                String paraValueStr = data.substring(1);
                String paraCmk = data.substring(0, 1);
                //参数关键字从0x60递增
                Integer number = Integer.parseInt(paraCmk,16)-95;
                List<FrameParaData> framParas = genFrameParaInfo(respData,paraInfos,paraValueStr);
                JSONObject object = new JSONObject();
                for (FrameParaData framPara : framParas) {
                    FrameParaInfo paraDetail = BaseInfoContainer.getParaInfoByNo(devType,framPara.getParaNo());
                    String paraName = paraDetail.getParaName();
                    String newName = "卫星"+ number;
                    if(paraName.contains("卫星")){
                        paraName.replace("卫星",newName);
                    }else{
                        paraName = newName+paraName;
                    }
                    object.put(paraName,framPara.getParaVal());
                }
                array.add(object);
                frameParaDataList.addAll(framParas);
            }
            respData.setPageQueryJsonStr(array.toJSONString());
        }
        respData.setFrameParaList(frameParaDataList);
        //参数查询响应结果接收
        dataReciveService.paraQueryRecive(respData);
        return respData;
    }


    /**
     * 生成数据帧参数数据
     * @param respData
     * @param paraInfos
     * @param paraValueStr
     */
    private List<FrameParaData> genFrameParaInfo(FrameRespData respData,List<FrameParaInfo> paraInfos,String paraValueStr){
        List<FrameParaData> frameParaDataList = new ArrayList<>();
        String devType = respData.getDevType();
        byte[] paraValBytes = HexUtil.decodeHex(paraValueStr);
        Integer startIndex = 0;
        for (FrameParaInfo paraInfo : paraInfos) {
            Integer endIndex = startIndex +  Integer.parseInt(paraInfo.getParaByteLen());
            if (StringUtils.isEmpty(paraInfo.getParaNo())){ continue;}
            FrameParaData frameParaData = FrameParaData.builder()
                    .devType(devType)
                    .devNo(respData.getDevNo())
                    .paraNo(paraInfo.getParaNo())
                    .build();
            //根据是否为String类型采取不同的处理方式
            boolean isStr = MonitorConstants.STRING_CODE.equals(paraInfo.getDataType());
            if (isStr){
                frameParaData.setParaVal(paraValueStr.substring(startIndex,endIndex));
            }else {

               Float paraVal = byteToNumber(paraValBytes, startIndex,
                        Integer.parseInt(paraInfo.getParaByteLen())
                        ,isUnsigned(sysParamService, paraInfo.getDataType())
                        ,isFloat(sysParamService, paraInfo.getDataType())).floatValue();
                String desc = paraInfo.getNdpaRemark2Desc();
                String data = paraInfo.getNdpaRemark3Data();
                if(StringUtils.isNotEmpty(desc) && desc.equals("倍数") && StringUtils.isNotEmpty(data)){
                    Integer multiple = Integer.parseInt(data);
                    paraVal = paraVal/multiple;
                }
                //单个参数值转换
                frameParaData.setParaVal(String.valueOf(paraVal));
            }
            startIndex = endIndex;
            frameParaDataList.add(frameParaData);
        }
        return frameParaDataList;
    }

}
