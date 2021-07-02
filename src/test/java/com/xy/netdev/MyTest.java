package com.xy.netdev;


import com.xy.netdev.common.util.SnmpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.smi.Variable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Author WangChao
 * @Description //TODO
 * @Date 18:10 2019/6/14
 * @Param
 * @return
 **/


public class MyTest {

    private static final String COMMUNITY_GET = "public";

    public static void main(String[] args) throws Exception {
        System.out.println("SNMP发送get请求....");

        String targetIp = "172.21.2.184";
        String oid2="1.3.6.1.4.1.63000.2.2.2.16.24.1.1.17";
        String oid1="1.3.6.1.4.1.63000.2.2.2.16.24.1.1.33";
        String oid3="1.3.6.1.4.1.63000.2.2.2.16.24.1.1.3";
        String oid4="1.3.6.1.4.1.63000.2.2.2.16.24.1.1.4";
        //String oid1="1.3.6.1.2.1.1.2.0";
//        String oid2="1.3.6.1.4.1.63000.2.2.2.145.3.1.1.11";
//        String oid3="1.3.6.1.4.1.63000.2.2.2.145.3.1.1.12";
//        String oid4="1.3.6.1.4.1.63000.2.2.2.145.3.1.1.13";
        List<String> oids = Arrays.asList(oid1, oid2,oid3,oid4);
       // List<String> oids = Arrays.asList(oid1);
//        Map<String, Variable> variableMap = SnmpUtil.snmpGet(targetIp, COMMUNITY_GET, oid1);
        Map<String, Variable> variableMap = SnmpUtil.snmpGetList(targetIp,COMMUNITY_GET,oids);

        System.out.println("收到SNMP消息响应：[{}]"+variableMap);

    }
}
