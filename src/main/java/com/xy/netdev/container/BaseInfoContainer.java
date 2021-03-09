package com.xy.netdev.container;

import com.xy.netdev.common.util.ParaHandlerUtil;
import com.xy.netdev.monitor.entity.BaseInfo;
import com.xy.netdev.monitor.entity.ParaInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *基础信息容器类
 * </p>
 *
 * @author tangxl
 * @since 2021-03-08
 */
public class BaseInfoContainer {
    /**
     * 设备MAP K设备IP地址 V设备信息
     */
    private static Map<String, BaseInfo> devMap = new HashMap<>();
    /**
     * 接口关联参数MAP K设备类型+接口编码 V设备参数列表信息
     */
    private static Map<String, List<ParaInfo>> InterLinkParaMap = new HashMap<>();

    /**
     * @功能：添加设备MAP
     * @param devList    设备列表
     * @return
     */
    public static void addDevMap(List<BaseInfo> devList) {

    }

    /**
     * @功能：添加接口关联参数MAP
     * @param paraList    参数列表
     * @return
     */
    public static void addInterLinkParaMap(List<ParaInfo> paraList) {

    }

    /**
     * @功能：根据设备IP地址 获取设备信息
     * @param devIPAddr    设备IP地址
     * @return  设备对象
     */
    public static BaseInfo   getDevInfo(String devIPAddr){
        return devMap.get(devIPAddr);
    }

    /**
     * @功能：根据设备类型  和  接口编码 获取接口解析参数列表
     * @param devType     设备类型
     * @param itfCode     接口编码
     * @return  接口解析参数列表
     */
    public static List<ParaInfo>   getInterLinkParaList(String devType,String itfCode){
        return InterLinkParaMap.get(ParaHandlerUtil.genLinkKey(devType,itfCode));
    }

}
