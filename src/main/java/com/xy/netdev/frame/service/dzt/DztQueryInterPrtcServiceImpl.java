package com.xy.netdev.frame.service.dzt;


import cn.hutool.core.util.HexUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.common.util.ByteUtils;
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

import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.xy.netdev.common.util.ByteUtils.byteArrayCopy;
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
    public static final String QUERY_RSP_ALL_MARK = "83";
    /** 单查询响应命令字*/
    public static final String QUERY_RSP_SINGLE_MARK = "85";
    /**查询应答帧分隔符*/
    private static final String SPLIT = "2c";


    @Override
    public void queryPara(FrameReqData reqInfo) {
        //暂时是单个参数查询 cmdMark为单个参数的命令标识
        if(reqInfo.getCmdMark().equals(QUERY_RSP_SINGLE_MARK)){
            reqInfo.setParamBytes(new byte[]{(byte) 0x01});
        }
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
        if(cmdMark.equals(QUERY_RSP_ALL_MARK)){
            for (String data : dataList) {
                String subInterfCmk = data.substring(0,2);
                String paraValueStr = data.substring(2);
                List<FrameParaInfo> paraInfos =  BaseInfoContainer.getInterLinkParaList(devType, subInterfCmk);
                frameParaDataList.addAll(genFrameParaInfo(respData,paraInfos,paraValueStr));
            }
        }else{
            List<FrameParaInfo> paraInfos =  BaseInfoContainer.getInterLinkParaList(devType, QUERY_RSP_SINGLE_MARK);
            JSONArray array = new JSONArray();
            for (String data : dataList) {
                String paraValueStr = data.substring(2);
                String paraCmk = data.substring(0, 2);
                //参数关键字从0x60递增
                Integer number = Integer.parseInt(paraCmk,16)-95;
                List<FrameParaData> framParas = genFrameParaInfo(respData,paraInfos,paraValueStr);
                JSONObject object = new JSONObject();
                for (FrameParaData framPara : framParas) {
                    FrameParaInfo paraDetail = BaseInfoContainer.getParaInfoByNo(devType,framPara.getParaNo());
                    String unit = paraDetail.getNdpaUnit();
                    String paraName = paraDetail.getParaName();
                    String newName = "卫星"+ number;
                    if(paraName.contains("卫星")){
                        paraName = paraName.replace("卫星",newName);
                    }else{
                        paraName = newName+paraName;
                    }
                    String val = framPara.getParaVal();
                    if (paraDetail.getSelectMap() != null && paraDetail.getSelectMap().size() > 0) {
                        val = (String) paraDetail.getSelectMap().get(val);
                    }
                    if(StringUtils.isNotEmpty(unit)){
                        val = val+" "+unit;
                    }
                    object.put(paraName,val);
                }
                array.add(object);
                frameParaDataList.addAll(framParas);
            }
            respData.setPageQueryJsonStr(array.toJSONString());
        }
        respData.setFrameParaList(frameParaDataList);
        //参数查询响应结果接收
        dataReciveService.interfaceQueryRecive(respData);
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
        Integer startIndex = 0;
        for (FrameParaInfo paraInfo : paraInfos) {
            List<FrameParaInfo> subParaList = paraInfo.getSubParaList();
            if (StringUtils.isEmpty(paraInfo.getParaNo())){ continue;}
            if(subParaList.size()==0){
                //解析简单参数
                FrameParaData frameParaData = FrameParaData.builder()
                        .devType(devType)
                        .devNo(respData.getDevNo())
                        .paraNo(paraInfo.getParaNo())
                        .build();
                startIndex = parseSingleParam(frameParaData,paraInfo,paraValueStr,startIndex);
                frameParaDataList.add(frameParaData);
            }else{
                //解析组合参数
                List<FrameParaData> frameParaDatas = new ArrayList<>();
                startIndex = parseCombinedParam(respData,paraInfo,paraValueStr,startIndex,frameParaDatas);
                frameParaDataList.addAll(frameParaDatas);
            }
        }
        return frameParaDataList;
    }

    /**
     * 根据倍数获取保留小数点后几位格式
     * @param multiple
     * @return
     */
    private String getDecimal(Integer multiple){
        String decimal = "";
        switch (multiple){
            case 10:
                decimal =  "###.0#";
                break;
            case 100:
                decimal =  "###.00#";
                break;
            case 1000:
                decimal =  "###.000#";
                break;
            default:
                decimal =  "###";
                break;
        }
        return decimal;
    }


    /**
     * 解析简单参数值
     * @param frameParaData
     * @param paraInfo
     * @param paraValueStr
     * @param startIndex
     * @return
     */
    private Integer  parseSingleParam(FrameParaData frameParaData,FrameParaInfo paraInfo,String paraValueStr,Integer startIndex) {
        //根据是否为String类型采取不同的处理方式
        byte[] paraValBytes = HexUtil.decodeHex(paraValueStr);
        Integer len = Integer.parseInt(paraInfo.getParaByteLen());
        Integer endIndex = startIndex + len ;
        boolean isStr = MonitorConstants.STRING_CODE.equals(paraInfo.getDataType());
        if (isStr) {
            frameParaData.setParaVal(paraValueStr.substring(startIndex * 2, endIndex * 2));
        } else {
            Number paraVal = byteToNumber(paraValBytes, startIndex,len
                    , isUnsigned(sysParamService, paraInfo.getDataType())
                    , isFloat(sysParamService, paraInfo.getDataType())).floatValue();
            String desc = paraInfo.getNdpaRemark1Desc();
            String data = paraInfo.getNdpaRemark1Data();
            String val = "";
            if (StringUtils.isNotEmpty(desc) && desc.equals("倍数") && StringUtils.isNotEmpty(data)) {
                Integer multiple = Integer.parseInt(data);
                //单个参数值转换
                DecimalFormat myFormatter = new DecimalFormat(getDecimal(multiple));
                val = myFormatter.format(paraVal.floatValue() / multiple);
            } else {
                val = String.valueOf(paraVal.intValue());
            }
            frameParaData.setParaVal(val);
        }
        startIndex = endIndex;
        return startIndex;
    }


    /**
     * 解析组合参数值
     * @param respData
     * @param paraInfo
     * @param paraValueStr
     * @param startIndex
     * @param frameParaDatas
     * @return
     */
    private Integer parseCombinedParam(FrameRespData respData,FrameParaInfo paraInfo,String paraValueStr,Integer startIndex,List<FrameParaData> frameParaDatas){
        byte[] paraValBytes = HexUtil.decodeHex(paraValueStr);
        Integer len = Integer.parseInt(paraInfo.getParaByteLen());
        Integer endIndex = startIndex + len ;
        String devType = respData.getDevType();
        Integer paraVal = byteToNumber(paraValBytes, startIndex,len
                , isUnsigned(sysParamService, paraInfo.getDataType())
                , isFloat(sysParamService, paraInfo.getDataType())).intValue();
        List<FrameParaInfo> subParaList = paraInfo.getSubParaList();
        if(subParaList.size()>0){
            String binaryStr =  Integer.toString(paraVal,2);
            int length = binaryStr.length();
            int j = 0;
            int k = 0;
            for (int i = subParaList.size(); i > 0; i--) {
                FrameParaInfo subParaInfo = subParaList.get(j);
                String val = "";
                if(i>length){
                    val = "0";
                }else{
                    val = String.valueOf(binaryStr.charAt(k));
                    k++;
                }
                j++;
                FrameParaData frameParaData = FrameParaData.builder().devType(devType).devNo(respData.getDevNo())
                        .paraNo(subParaInfo.getParaNo()).paraVal(val).build();
                frameParaDatas.add(frameParaData);
            }
        }
        startIndex = endIndex;
        return startIndex;
    }
}
