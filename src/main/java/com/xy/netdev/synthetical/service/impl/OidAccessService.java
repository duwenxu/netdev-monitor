package com.xy.netdev.synthetical.service.impl;

import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.frame.service.snmp.SnmpRptDTO;
import com.xy.netdev.synthetical.factory.OidHandlerFactory;
import com.xy.netdev.synthetical.service.IOidAccessService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    private final ConcurrentHashMap<String, SnmpRptDTO> mergeSnmpDtoMap= new ConcurrentHashMap<>(10);
    private static final String STOP_SIGN_OID = "1.3.6.1.4.1.63000.2.2.2.00.00.1.1.4";

    @Override
    public List<VariableBinding> getVariablesByOidList(List<String> oidList) {
        long t1 = System.currentTimeMillis();
        Map<String, Map<String, SnmpRptDTO>> devSnmpParaMap = DevParaInfoContainer.getDevSnmpParaMap();
        /**合并各个设备的OID参数*/
        mergeSnmpDtoMap.clear();
        for (Map<String, SnmpRptDTO> rptDTOMap : devSnmpParaMap.values()) {
                mergeSnmpDtoMap.putAll(rptDTOMap);
        }

        long t2 = System.currentTimeMillis();
        ArrayList<VariableBinding> variableBindings = new ArrayList<>();
        for (String oid : oidList) {
            if (oid.endsWith(".1.1.1")){
                VariableBinding binding = new VariableBinding(new OID(STOP_SIGN_OID), new Integer32(1));
                variableBindings.add(binding);
            }else {
                /**OID拼接后缀发送*/
                oid = oid + DevParaInfoContainer.SNMP_RPT_SUFFIX;
                VariableBinding variableBinding = new VariableBinding(new OID(oid));
                if (!mergeSnmpDtoMap.containsKey(oid)) {
                    variableBinding.setVariable(new Null());
                } else {
                    SnmpRptDTO snmpRptDTO = mergeSnmpDtoMap.get(oid);
                    String paraVal = snmpRptDTO.getParaVal();
                    if (StringUtils.isBlank(paraVal)) {
                        variableBinding.setVariable(new Null());
                    } else {
                        String dataType = snmpRptDTO.getParaDatatype();
                        if (INT.equals(dataType) || UNIT.equals(dataType)) {
                            variableBinding.setVariable(new Integer32(Integer.parseInt(paraVal)));
                        } else if (IP_ADDRESS.equals(dataType) || IP_MASK.equals(dataType)) {
                            variableBinding.setVariable(new IpAddress(paraVal));
                        } else if (BYTE.equals(dataType)) {
                            /**byte类型的16进制需要转换为10进制数*/
                            String octVal = Integer.parseInt(paraVal, 16) + "";
                            variableBinding.setVariable(new Integer32(Integer.parseInt(octVal)));
                        } else {
                            variableBinding.setVariable(new OctetString(paraVal));
                        }
                    }
                }
                variableBindings.add(variableBinding);
            }
        }
        log.debug("单次获取所有OID的数据耗时: 合并阶段：[{}]---遍历获取阶段：[{}]", t2 - t1, System.currentTimeMillis() - t2);
        return variableBindings;
    }
}
