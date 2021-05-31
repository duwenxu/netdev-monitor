package com.xy.netdev.frame.service.snmp;

import cn.hutool.core.bean.BeanUtil;
import com.xy.netdev.common.util.SnmpUtil;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.smi.Variable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * SNMP协议接口查询实现
 */
@Service
@Slf4j
public class SnmpTransceiverServiceImpl implements SnmpTransceiverService {
    private static final String COMMUNITY = "public";

    @Override
    public SnmpResDTO queryParam(SnmpReqDTO snmpReqDTO,String baseIp) {
        String reqOid = snmpReqDTO.getOid();

        Map<String, Variable> snmpMap = SnmpUtil.snmpGet(baseIp, COMMUNITY, reqOid);
        Variable variable = snmpMap.get(reqOid);
        Assert.isTrue(variable !=null,"SNMP请求失败不存在当前oid:["+reqOid+"]的结果");

        SnmpResDTO snmpResDTO = new SnmpResDTO();
        BeanUtil.copyProperties(snmpReqDTO, snmpResDTO, true);
        snmpResDTO.setParaVal(variable.toString());
        return snmpResDTO;
    }

    @Override
    public SnmpResDTO queryParamList(SnmpReqDTO snmpReqDTO, String baseIp) {
        return null;
    }
}
