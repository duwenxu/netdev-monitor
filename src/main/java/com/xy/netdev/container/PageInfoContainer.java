package com.xy.netdev.container;


import com.xy.common.exception.BaseException;
import com.xy.netdev.common.util.ParaHandlerUtil;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *自定义页面信息容器类
 * </p>
 *
 * @author tangxl
 * @since 2021-03-30
 */
public class PageInfoContainer {
    /**
     * 页面信息MAP K设备编号+页面查询接口编码 V页面缓存信息
     */
    private static Map<String,String> pageInfoMap = new HashMap<>();

    /**
     * @功能：添加页面缓存信息
     * @param devNo           设备编号
     * @param interCode       接口编码
     * @param contextStr      设备告警信息
     * @return 是否变化 true 变化  false 未变化
     */
    public synchronized static boolean addPageInfo(String devNo,String interCode,String contextStr) {
        String key = ParaHandlerUtil.genLinkKey(devNo, interCode);
        if(pageInfoMap.containsKey(key)){
            if(!pageInfoMap.get(key).equals(contextStr)){
                pageInfoMap.put(key,contextStr);
                return true;
            }
            return false;
        }else{
            pageInfoMap.put(key,contextStr);
            return true;
        }
    }



    /**
     * @功能：根据设备编号 和 接口编码 获取最新的自定义页面缓存信息
     * @param devNo           设备编号
     * @param interCode       接口编码
     * @return  自定义页面缓存信息
     */
    public static String  getPageInfo(String devNo,String interCode){
        String key = ParaHandlerUtil.genLinkKey(devNo, interCode);
        if(pageInfoMap.containsKey(key)){
            return pageInfoMap.get(key);
        }
        throw new BaseException("设备编号:"+devNo+" 页面接口编码:"+interCode+"没有页面信息!");
    }
}
