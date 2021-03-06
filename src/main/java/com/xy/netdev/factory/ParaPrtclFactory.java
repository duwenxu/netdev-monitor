package com.xy.netdev.factory;



import com.xy.netdev.common.util.SpringContextUtils;
import com.xy.netdev.frame.service.IParaPrtclAnalysisService;



/**
 * <p>
 * 参数协议解析工厂类
 * </p>
 *
 * @author tangxl
 * @since 2021-03-10
 */
public class ParaPrtclFactory {
    /**
     * @功能：生成具体的处理类  传入为空时，返回缺省的处理类
     * @param  handlerMark           处理服务名
     * @return
     */
    public static IParaPrtclAnalysisService genHandler(String handlerMark){
       return SpringContextUtils.getBean(handlerMark);
    }
}
