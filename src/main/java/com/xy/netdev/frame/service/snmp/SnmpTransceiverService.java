package com.xy.netdev.frame.service.snmp;


/**
 * SNMP收发聚合处理接口
 */
public interface SnmpTransceiverService {

    /**
     * 单参数查询
     * @param snmpReqDTO 查询参数体
     * @param baseIp 查询IP
     * @return 查询结构DTO
     */
    SnmpResDTO queryParam(SnmpReqDTO snmpReqDTO,String baseIp);

    /**
     * 多参数查询
     * @param snmpReqDTO 查询参数体
     * @param baseIp 查询IP
     * @return 查询结构DTO
     */
    SnmpResDTO queryParamList(SnmpReqDTO snmpReqDTO,String baseIp);

    /**
     * 单参数 控制
     * @param snmpReqDTO 控制参数体
     * @param baseIp 控制IP
     */
    void paramCtrl(SnmpReqDTO snmpReqDTO,String baseIp);
}
