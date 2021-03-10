package com.xy.netdev.factory;

import com.xy.netmgr.common.util.SpringContextUtils;
import com.xy.netmgr.iot.handler.IApplyCfmHandler;
import org.apache.commons.lang.StringUtils;


/**
 * <p>
 * 申请确认工厂类
 * </p>
 *
 * @author tangxl
 * @since 2021-01-18
 */
public class ApplyCfmFactory {
    /**
     * @功能：生成具体的处理类  传入为空时，返回缺省的处理类
     * @param  handlerMark           处理服务名
     * @return
     */
    public static IApplyCfmHandler genHandler(String handlerMark){
        if(StringUtils.isEmpty(handlerMark)){
            return SpringContextUtils.getBean("defaultApplyCfmHandler");
        }
       return SpringContextUtils.getBean(handlerMark);
    }
}
