package com.xy.netdev.synthetical.agent;


import org.snmp4j.agent.mo.DefaultMOTable;
import org.snmp4j.agent.mo.MOColumn;
import org.snmp4j.agent.mo.MOTableIndex;
import org.snmp4j.smi.OID;

/**
 * <p>
 * snmp table 把各个设备封装成表的基础类
 * </p>
 *
 * @author tangxl
 * @since 2021-06-21
 */
public class XySnmpTable extends  DefaultMOTable {

    private String devNo;


    public String getDevNo() {
        return devNo;
    }

    public void setDevNo(String devNo) {
        this.devNo = devNo;
    }


    public XySnmpTable(OID oid, MOTableIndex indexDef, MOColumn[] columns) {
        super(oid, indexDef, columns);
    }
}
