package com.xy.netdev.synthetical.factory;



import com.xy.common.exception.BaseException;
import com.xy.netdev.common.util.SpringContextUtils;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.container.paraext.IParaExtService;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.synthetical.service.IOidHandlerService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;


/**
 * <p>
 * 综合网管访问处理服务工厂类
 * </p>
 *
 * @author tangxl
 * @since 2021-06-16
 */
@Service
public class OidHandlerFactory {
    /**
     * @功能：生成具体的综合网管访问处理服务类
     * @param  oid           设备类型
     * @return
     */
    public static IOidHandlerService getValByOid(String oid){
        ParaInfo paraInfo = DevParaInfoContainer.getOidParaIno(oid);
        String devType  = paraInfo.getDevType();
        if(StringUtils.isEmpty(devType)){
            throw new BaseException("传入的设备类型为空!");
        }
        if(devType.equals("具体的设备类型")){
            return  SpringContextUtils.getBean("设备类型对应的服务类");
        }
        //没有实现扩展的设备类型走此扩展  保持原来的逻辑
        return SpringContextUtils.getBean("doNothingOidHandlerService");
    }
}
