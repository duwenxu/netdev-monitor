package com.xy.netdev.container;

import com.alibaba.fastjson.JSONArray;
import com.xy.netdev.common.util.ParaHandlerUtil;
import com.xy.netdev.frame.bo.FrameParaData;
import com.xy.netdev.frame.bo.FrameRespData;
import com.xy.netdev.monitor.bo.ParaSpinnerInfo;
import com.xy.netdev.monitor.bo.ParaViewInfo;
import com.xy.netdev.monitor.entity.ParaInfo;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *各设备参数缓存容器类
 * </p>
 *
 * @author tangxl
 * @since 2021-03-08
 */
public class DevParaInfoContainer {

    /**
     * 设备参数MAP K设备编号  V设备参数信息
     */
    private static Map<String, Map<String, ParaViewInfo>> devParaMap = new HashMap<>();


    /**
     * @功能：添加设备参数MAP
     * @param paraList  参数列表
     * @return
     */
    public static void initData(List<ParaInfo> paraList) {
        Map<String,List<ParaInfo>> paraMapByDevType = paraList.stream().collect(Collectors.groupingBy(ParaInfo::getDevType));
        BaseInfoContainer.getDevNos().forEach(devNo->{
            String devType = BaseInfoContainer.getDevInfoByNo(devNo).getDevType();
            devParaMap.put(devNo,assembleViewList(devNo,paraMapByDevType.get(devType)));
        });
    }
    /**
     * @功能：根据设备类型对应的参数信息  生成设备显示列表
     * @param devNo            设备编号
     * @param devTypeParaList  设备类型参数列表
     * @return 设备显示列表
     */
    private static Map<String,ParaViewInfo> assembleViewList(String devNo,List<ParaInfo> devTypeParaList){
        Map<String,ParaViewInfo>  paraViewMap = new HashMap<>();
        if(devTypeParaList!=null&&!devTypeParaList.isEmpty()){
            for(ParaInfo paraInfo:devTypeParaList){
                paraViewMap.put(ParaHandlerUtil.genLinkKey(devNo,paraInfo.getNdpaNo()),genParaViewInfo(devNo,paraInfo));
            }
        }
        return paraViewMap;
    }

    private static ParaViewInfo  genParaViewInfo(String devNo,ParaInfo paraInfo){
        ParaViewInfo  viewInfo = new ParaViewInfo();
        viewInfo.setParaId(paraInfo.getNdpaId());
        viewInfo.setParaNo(paraInfo.getNdpaNo());
        viewInfo.setParaCode(paraInfo.getNdpaCode());
        viewInfo.setParaName(paraInfo.getNdpaName());
        viewInfo.setParaCmdMark(paraInfo.getNdpaCmdMark());
        viewInfo.setAccessRight(paraInfo.getNdpaAccessRight());
        viewInfo.setParaUnit(paraInfo.getNdpaUnit());
        viewInfo.setParaDatatype(paraInfo.getNdpaDatatype());
        viewInfo.setParaStrLen(paraInfo.getNdpaStrLen());
        viewInfo.setParahowMode(paraInfo.getNdpaShowMode());
        viewInfo.setParaValMax(paraInfo.getNdpaValMax());
        viewInfo.setParaValMin(paraInfo.getNdpaValMin());
        viewInfo.setParaValStep(paraInfo.getNdpaValStep());
        viewInfo.setDevNo(devNo);
        viewInfo.setDevType(paraInfo.getDevType());
        viewInfo.setSpinnerInfoList(JSONArray.parseArray(paraInfo.getNdpaSelectData(),ParaSpinnerInfo.class));
        return viewInfo;
    }


    /**
     * @功能：根据设备显示参数列表
     * @param devNo        设备编号
     * @return  设备显示参数列表
     */
    public static List<ParaViewInfo>   getDevParaViewList(String devNo){
        return new ArrayList(devParaMap.get(devNo).values());
    }

    /**
     * @功能：设置设备响应参数信息
     * @param respData        协议解析响应数据
     * @return 数据是否发生变化
     */
    public static boolean   handlerRespDevPara(FrameRespData respData){
        List<FrameParaData> frameParaList = respData.getFrameParaList();
        boolean isUpadte = false;
        if(frameParaList!=null&&!frameParaList.isEmpty()){
            for(FrameParaData frameParaData:frameParaList) {
                String devNo = frameParaData.getDevNo();
                String paraNo = frameParaData.getParaNo();
                ParaViewInfo paraViewInfo = devParaMap.get(devNo).get(ParaHandlerUtil.genLinkKey(devNo, paraNo));
                if (!paraViewInfo.getParaVal().equals(frameParaData.getParaVal())) {
                    paraViewInfo.setParaVal(frameParaData.getParaVal());
                    isUpadte = true;
                }
            }
        }
        return isUpadte;
    }

}
