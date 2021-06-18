package com.xy.netdev.synthetical.bo;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.BaseInfoContainer;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.container.DevStatusContainer;
import com.xy.netdev.monitor.bo.DevStatusInfo;
import com.xy.netdev.monitor.entity.ParaInfo;
import com.xy.netdev.synthetical.factory.OidHandlerFactory;
import com.xy.netdev.synthetical.util.SyntheticalUtil;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.smi.Integer32;
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

    public OidParaInfo(String oid,V v){
        super(new OID(oid), MOAccessImpl.ACCESS_READ_ONLY,v);
        this.oidStr = oid;
    }

    public Variable getValue() {
        if(DevParaInfoContainer.containsOid(oidStr)){
            Integer  oidNo = Integer.parseInt(oidStr.substring(oidStr.lastIndexOf(".")+1));
            if(oidNo>4){
                ParaInfo paraInfo = DevParaInfoContainer.getOidParaIno(oidStr);
                return SyntheticalUtil.genSnmpVariable(paraInfo.getNdpaDatatype(),OidHandlerFactory.getValByOid(oidStr).getValByOid(oidStr));
            }
            ISysParamService sysParamService = BaseInfoContainer.getSysParamService();
            if(oidNo ==1){
                return new Integer32(Integer.parseInt(sysParamService.getParaRemark1(SysConfigConstant.PRIVATE_MIB_REGION)));
            }
            if(oidNo ==2){
                return new Integer32(Integer.parseInt(sysParamService.getParaRemark1(SysConfigConstant.PRIVATE_MIB_STATION)));
            }
            if(oidNo ==3){
                return new Integer32(Integer.parseInt(DevParaInfoContainer.getOidDevNo(oidStr)));
            }
            if(oidNo ==4){
                DevStatusInfo devStatusInfo = DevStatusContainer.getDevStatusInfo(DevParaInfoContainer.getOidDevNo(oidStr));
                if(devStatusInfo.getIsInterrupt().equals("0")){
                    return new Integer32(1);
                }
                return new Integer32(0);
            }

        }
        return new OctetString("");
    }
}


