package com.xy.netdev.frame.service.snmp;


/**
 * SNMP收发聚合接口
 */
public interface SnmpTransceiverService {

    SnmpResDTO queryParam(SnmpReqDTO snmpReqDTO);

    SnmpResDTO queryParamList(SnmpReqDTO snmpReqDTO);
}
