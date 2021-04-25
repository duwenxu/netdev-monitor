package com.xy.netdev.container.interext;



import com.xy.common.exception.BaseException;
import com.xy.netdev.common.util.SpringContextUtils;
import org.apache.commons.lang.StringUtils;


/**
 * <p>
 * 参数缓存扩展工厂类
 * </p>
 *
 * @author tangxl
 * @since 2021-04-25
 */
public class InterExtServiceFactory {
    /**
     * @功能：生成具体的参数缓存扩展类
     * @param  devType           设备类型
     * @return
     */
    public static InterExtService genParaExtService(String devType){
       if(StringUtils.isEmpty(devType)){
           throw new BaseException("传入的设备类型为空!");
       }
       if(devType.equals("设备类型编码")){
           SpringContextUtils.getBean("服务名");
       }
       //没有实现扩展的设备类型走此扩展  保持原来的逻辑
       return SpringContextUtils.getBean("doNothingInterExtService");
    }
}
