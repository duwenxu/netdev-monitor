package com.xy.netdev.synthetical.service.impl;


import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.synthetical.factory.OidHandlerFactory;
import com.xy.netdev.synthetical.service.IOidAccessService;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 综合网管访问服务实现
 *
 * @author tangxl
 * @date 2021-06-16
 */
@Service
public class OidAccessService implements IOidAccessService {

    /**
     * 获取指定OID的参数值
     * @param oid            设备参数OID
     * @return 设备参数OID 与 设备参数值 MAP
     */
    public Map<String,String> getValByOid(String oid) {
        Map<String,String> result = new HashMap<>();
        genOidVal(oid,result);
        return result;
    }


    /**
     * 获取OID列表的参数值
     * @param oidList            设备参数OID列表
     * @return 设备参数OID 与 设备参数值 MAP
     */
    public Map<String,String> getValByOidList(List<String> oidList) {
        Map<String,String> result = new HashMap<>();
        for(String oid:oidList){
            genOidVal(oid,result);
        }
        return result;
    }

    private void genOidVal(String oid,Map<String,String> result){
        if(DevParaInfoContainer.containsOid(oid)){
            result.put(oid, OidHandlerFactory.getValByOid(oid).getValByOid(oid));
        }else{
            result.put(oid,"");
        }
    }

}
