package com.xy.netdev.transit;

import com.xy.netdev.frame.service.snmp.SnmpResDTO;

/**
 * SNMP 数据接收  将获取到的SNMP数据填充到内存数据缓存
 */
public interface ISnmpDataReceiveService {

    /**
     * 参数查询接收
     * @param  snmpResDTO   协议解析响应数据
     */
    void paraQueryRecive(SnmpResDTO snmpResDTO);
}
