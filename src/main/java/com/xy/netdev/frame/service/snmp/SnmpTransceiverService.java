package com.xy.netdev.frame.service.snmp;


/**
 * SNMP收发聚合处理接口
 */
public interface SnmpTransceiverService {

    SnmpResDTO queryParam(SnmpReqDTO snmpReqDTO,String baseIp);

    SnmpResDTO queryParamList(SnmpReqDTO snmpReqDTO,String baseIp);
}
