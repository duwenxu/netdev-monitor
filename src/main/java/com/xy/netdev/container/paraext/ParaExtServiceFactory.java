package com.xy.netdev.container.paraext;



import com.xy.common.exception.BaseException;
import com.xy.netdev.common.util.SpringContextUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;


/**
 * <p>
 * 参数缓存扩展工厂类
 * </p>
 *
 * @author tangxl
 * @since 2021-04-25
 */
@Service
public class ParaExtServiceFactory {

    /*多体制卫星信道终端*/
    private static final String DEV_TYPE_MSCT = "0020015";
    /**
     * @功能：生成具体的参数缓存扩展类
     * @param  devType           设备类型
     * @return
     */
    public static IParaExtService genParaExtService(String devType){
       if(StringUtils.isEmpty(devType)){
           throw new BaseException("传入的设备类型为空!");
       }
        if(devType.equals(DEV_TYPE_MSCT)){
            SpringContextUtils.getBean("msctParaExtService");
        }
        //没有实现扩展的设备类型走此扩展  保持原来的逻辑
        return SpringContextUtils.getBean("doNothingParaExtService");
    }
}
