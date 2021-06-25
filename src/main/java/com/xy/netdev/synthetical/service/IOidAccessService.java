package com.xy.netdev.synthetical.service;



import java.util.List;
import java.util.Map;


/**
 * 综合网管访问服务接口
 *
 * @author tangxl
 * @date 2021-06-16
 */
public interface IOidAccessService {

    /**
     * 获取指定OID的参数值
     * @param oid            设备参数OID
     * @return 设备参数OID 与 设备参数值 MAP
     */
    Map<String,String> getValByOid(String oid);


    /**
     * 获取OID列表的参数值
     * @param oidList            设备参数OID列表
     * @return 设备参数OID 与 设备参数值 MAP
     */
    Map<String,String> getValByOidList(List<String> oidList);

    /**
     * 获取传入oid的next值
     * @param oidList 传入的oidList
     * @return oid--value
     */
    Map<String,String> getNextByOid(List<String> oidList);


}
