package com.xy.netdev.synthetical.bo;

import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.synthetical.factory.OidHandlerFactory;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

/**
 * <p>
 * 设备参数OID对象
 * </p>
 *
 * @author tangxl
 * @since 2021-06-17
 */

public class OidParaInfo<V extends Variable> extends MOScalar {

    private String oidStr;

    public OidParaInfo(String oid){
        super(new OID(oid), MOAccessImpl.ACCESS_READ_ONLY,new OctetString(""));
        this.oidStr = oid;
    }

    public OctetString getValue() {
        if(DevParaInfoContainer.containsOid(oidStr)){
            return new OctetString(OidHandlerFactory.getValByOid(oidStr).getValByOid(oidStr));
        }
        return new OctetString("");
    }
}


