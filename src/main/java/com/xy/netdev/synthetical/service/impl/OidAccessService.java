package com.xy.netdev.synthetical.service.impl;

import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.frame.service.snmp.SnmpRptDTO;
import com.xy.netdev.synthetical.factory.OidHandlerFactory;
import com.xy.netdev.synthetical.service.IOidAccessService;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.smi.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.xy.netdev.monitor.constant.MonitorConstants.*;


/**
 * 综合网管访问服务实现
 *
 * @author tangxl
 * @date 2021-06-16
 */
@Service
@Slf4j
public class OidAccessService implements IOidAccessService {

    /**
     * 获取指定OID的参数值
     *
     * @param oid 设备参数OID
     * @return 设备参数OID 与 设备参数值 MAP
     */
    public Map<String, String> getValByOid(String oid) {
        Map<String, String> result = new HashMap<>();
        genOidVal(oid, result);
        return result;
    }


    /**
     * 获取OID列表的参数值
     *
     * @param oidList 设备参数OID列表
     * @return 设备参数OID 与 设备参数值 MAP
     */
    public Map<String, String> getValByOidList(List<String> oidList) {
        Map<String, String> result = new HashMap<>();
        for (String oid : oidList) {
            genOidVal(oid, result);
        }
        return result;
    }

    private void genOidVal(String oid, Map<String, String> result) {
        if (DevParaInfoContainer.containsOid(oid)) {
            result.put(oid, OidHandlerFactory.getValByOid(oid).getValByOid(oid));
        } else {
            result.put(oid, "");
        }
    }

    @Override
    public List<VariableBinding> getVariablesByOidList(List<String> oidList) {
        Map<String, Map<String, SnmpRptDTO>> devSnmpParaMap = DevParaInfoContainer.getDevSnmpParaMap();
        ConcurrentHashMap<String, SnmpRptDTO> mergeSnmpDtoMap = new ConcurrentHashMap<>();
        for (Map<String, SnmpRptDTO> rptDTOMap : devSnmpParaMap.values()) {
            for (Map.Entry<String, SnmpRptDTO> dtoEntry : rptDTOMap.entrySet()) {
                String dtoEntryKey = dtoEntry.getKey();
                if (mergeSnmpDtoMap.containsKey(dtoEntryKey)) {
                    log.error("存在重复的参数上报OID：参数1：[{}]--参数2:[{}]", dtoEntry.getValue(), mergeSnmpDtoMap.get(dtoEntryKey));
                } else {
                    mergeSnmpDtoMap.put(dtoEntryKey, dtoEntry.getValue());
                }
            }
        }

        ArrayList<VariableBinding> variableBindings = new ArrayList<>();
        for (String oid : oidList) {
            VariableBinding variableBinding = new VariableBinding(new OID(oid));
            if (!mergeSnmpDtoMap.containsKey(oid)) {
                variableBinding.setVariable(new Null());
            } else {
                SnmpRptDTO snmpRptDTO = mergeSnmpDtoMap.get(oid);
                String dataType = snmpRptDTO.getParaDatatype();
                String paraVal = snmpRptDTO.getParaVal();
                if (INT.equals(dataType) || UNIT.equals(dataType) || BYTE.equals(dataType)) {
                    variableBinding.setVariable(new Integer32(Integer.parseInt(paraVal)));
                } else if (IP_ADDRESS.equals(dataType) || IP_MASK.equals(dataType)) {
                    variableBinding.setVariable(new IpAddress(paraVal));
                } else if (STR.equals(dataType)) {
                    variableBinding.setVariable(new OctetString(paraVal));
                }
            }
            variableBindings.add(variableBinding);
        }
        return variableBindings;
    }
}
