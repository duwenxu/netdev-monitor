package com.xy.netdev.container;


import com.xy.netdev.container.interext.InterExtServiceFactory;
import com.xy.netdev.monitor.bo.*;
import com.xy.netdev.monitor.entity.Interface;
import java.util.*;


/**
 * <p>
 *各设备控制接口信息容器类
 * </p>
 *
 * @author tangxl
 * @since 2021-04-06
 */
public class DevCtrlInterInfoContainer {
    /**
     * 设备参数MAP K设备编号  V设备组装控制接口
     */
    private static Map<String,List<InterfaceViewInfo>> devCtrInterMap = new HashMap<>();


    /**
     * @功能：初始化 各设备控制接口信息容器
     * @return
     */
    public static void initData() {
        BaseInfoContainer.getDevNos().forEach(devNo->{
            devCtrInterMap.put(devNo,assembleViewList(devNo));
            String devType = BaseInfoContainer.getDevInfoByNo(devNo).getDevType();
            InterExtServiceFactory.genParaExtService(devType).setCacheDevInterViewInfo(devNo);
        });
    }

    /**
     * @功能：根据设备类型对应的参数信息  生成设备显示列表
     * @param devNo            设备编号
     * @return 设备显示列表
     */
    private static List<InterfaceViewInfo> assembleViewList(String devNo){
        List<InterfaceViewInfo>  devCtrlInterList = new ArrayList<>();
        BaseInfoContainer.getCtrlItfInfo(devNo).forEach(anInterface -> {
            devCtrlInterList.add(genInter(devNo,anInterface));
        });
        return devCtrlInterList;
    }

    /**
     * @功能：生成展示的接口信息,若有子接口需递归
     * @param devNo            设备编号
     * @param inter            设备接口信息
     * @return 展示的接口信息
     */
    private static InterfaceViewInfo genInter(String devNo,Interface inter){
        InterfaceViewInfo  interfaceViewInfo  = genInterBaseInfo(devNo,inter);
        genInterParaInfo(devNo,interfaceViewInfo);
        genSubInterList(devNo,interfaceViewInfo);
        return interfaceViewInfo;
    }

    /**
     * @功能：生成展示接口基本信息
     * @param devNo            设备编号
     * @param inter            设备接口信息
     * @return 展示的接口信息
     */
    private static InterfaceViewInfo  genInterBaseInfo(String devNo,Interface inter){
        InterfaceViewInfo  interfaceViewInfo  = new InterfaceViewInfo();
        interfaceViewInfo.setDevNo(devNo);
        interfaceViewInfo.setDevType(inter.getDevType());
        interfaceViewInfo.setItfCmdMark(inter.getItfCmdMark());
        interfaceViewInfo.setItfCode(inter.getItfCode());
        interfaceViewInfo.setItfName(inter.getItfName());
        interfaceViewInfo.setItfType(inter.getItfType());
        return interfaceViewInfo;
    }

    /**
     * @功能：生成展示接口的参数信息
     * @param devNo                         设备编号
     * @param interfaceViewInfo             设备接口展示信息
     * @return
     */
    private static void  genInterParaInfo(String devNo,InterfaceViewInfo  interfaceViewInfo){
        List<ParaViewInfo> subParaList = new ArrayList<>();
        BaseInfoContainer.getInterLinkParaList(interfaceViewInfo.getDevType(),interfaceViewInfo.getItfCmdMark()).forEach(frameParaInfo -> {
            ParaViewInfo  viewInfo = DevParaInfoContainer.getDevParaView(devNo,frameParaInfo.getParaNo());
            if(viewInfo!=null){
                subParaList.add(viewInfo);
            }
        });
        interfaceViewInfo.setSubParaList(subParaList);
    }


    /**
     * @功能：生成展示接口的子接口列表信息
     * @param devNo                         设备编号
     * @param interfaceViewInfo             设备接口展示信息
     * @return
     */
    private static void  genSubInterList(String devNo,InterfaceViewInfo  interfaceViewInfo){
        List<InterfaceViewInfo> subInterList = new ArrayList<>();
        BaseInfoContainer.getSubIftList(interfaceViewInfo.getDevType(),interfaceViewInfo.getItfCmdMark()).forEach(devInterParam -> {
            subInterList.add(genInter(devNo,devInterParam.getDevInterface()));
        });
        interfaceViewInfo.setSubInterList(subInterList);
    }


    /**
     * @功能：根据设备显示组装控制接口列表
     * @param devNo        设备编号
     * @return  组装控制接口列表
     */
    public static List<InterfaceViewInfo>   getDevCtrInterList(String devNo){
        String devType = BaseInfoContainer.getDevInfoByNo(devNo).getDevType();
        return InterExtServiceFactory.genParaExtService(devType).getCacheDevInterViewInfo(devNo);
    }

    /**
     * @功能：根据设备显示组装控制接口列表
     * @param devNo        设备编号
     * @return  组装控制接口列表
     */
    public static List<InterfaceViewInfo>   getDevCtrInterExtList(String devNo){
        return devCtrInterMap.get(devNo);
    }

    /**
     * 清空缓存
     */
    public static void cleanCache(){
        devCtrInterMap.clear();
    }

}
