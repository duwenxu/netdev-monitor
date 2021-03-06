package com.xy.netdev.synthetical;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class SnmpAddressConfig {
    @Value(value = "${snmp.listenAddress}")
    private String listenAddress;

    @Value(value = "${snmp.targetAddress}")
    private String targetAddress;
}
