package com.xy.netdev.synthetical.service;



import java.util.Map;


/**
 * 综合网管访问处理服务接口
 *
 * @author tangxl
 * @date 2021-06-16
 */
public interface IOidHandlerService {

    /**
     * 获取指定OID的参数值
     * @param oid            设备参数OID
     * @return 设备参数OID 与 设备参数值 MAP
     */
    String getValByOid(String oid);


}
