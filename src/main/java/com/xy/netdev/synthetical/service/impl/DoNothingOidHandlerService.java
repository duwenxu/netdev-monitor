package com.xy.netdev.synthetical.service.impl;



import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.synthetical.service.IOidHandlerService;
import org.springframework.stereotype.Service;



/**
 * 综合网管访问处理服务接口通用实现
 *
 * @author tangxl
 * @date 2021-06-16
 */
@Service
public class DoNothingOidHandlerService implements IOidHandlerService {

    /**
     * 获取指定OID的参数值
     * @param oid            设备参数OID
     * @return 设备参数OID 与 设备参数值 MAP
     */
    public String getValByOid(String oid) {
        if(DevParaInfoContainer.containsOid(oid)){
            ParaInfo paraInfo = DevParaInfoContainer.getOidParaIno(oid);
            return DevParaInfoContainer.getDevParaView(paraInfo.getDevNo(),paraInfo.getNdpaNo()).getParaVal();
        }
        return "";
    }


}
