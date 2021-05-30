package com.xy.netdev.frame.service.snmp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 被复线SNMP接口查询实现
 */
@Service
@Slf4j
public class DoubleLineTransceiverServiceImpl implements SnmpTransceiverService {

    @Override
    public SnmpResDTO queryParam(SnmpReqDTO snmpReqDTO) {
        return null;
    }

    @Override
    public SnmpResDTO queryParamList(SnmpReqDTO snmpReqDTO) {
        return null;
    }
}
