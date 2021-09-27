package com.xy.netdev.common.util;

import lombok.extern.slf4j.Slf4j;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.xy.netdev.common.constant.SysConfigConstant.FAIL;
import static com.xy.netdev.common.constant.SysConfigConstant.SUCCESS;

/**
 * SNMP协议操作工具包
 */
@Slf4j
public class SnmpUtil {

    public static final int DEFAULT_VERSION = SnmpConstants.version2c;
    public static final String DEFAULT_PROTOCOL = "udp";
    public static final int DEFAULT_PORT = 161;
    public static final long DEFAULT_TIMEOUT = 3 * 1000L;
    public static final int DEFAULT_RETRY = 3;
    //trap端口
    public static final int DEFAULT_TRAP_PORT = 162;

    /**
     * 创建对象communityTarget，用于返回target
     *
     * @param community
     * @return CommunityTarget
     */
    public static CommunityTarget createDefault(String ip, String community) {
        Address address = GenericAddress.parse(DEFAULT_PROTOCOL + ":" + ip
                + "/" + DEFAULT_PORT);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(address);
        target.setVersion(DEFAULT_VERSION);
        target.setTimeout(DEFAULT_TIMEOUT); // milliseconds
        target.setRetries(DEFAULT_RETRY);
        return target;
    }

    /**
     * 根据OID获取单条数据
     *
     * @param ip        设备IP
     * @param community 设备组织
     * @param oid       参数数据标识
     */
    public static Map<String, Variable> snmpGet(String ip, String community, String oid) {
        ConcurrentHashMap<String, Variable> values = new ConcurrentHashMap<>(1);
        CommunityTarget target = createDefault(ip, community);
        Snmp snmp = null;
        try {
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid)));

            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            snmp.listen();
            pdu.setType(PDU.GET);
            ResponseEvent respEvent = snmp.send(pdu, target);
            log.debug("SNMP发送PDU类型：[{}]到地址：[{}]---", pdu.getType(), respEvent.getPeerAddress());
            PDU response = respEvent.getResponse();
            if (response == null) {
                log.debug("SNMP response is null, request time out");
            } else {
                log.debug("SNMP response pdu size is " + response.size());
                for (int i = 0; i < response.size(); i++) {
                    VariableBinding vb = response.get(i);
                    values.put(vb.getOid().toString(), vb.getVariable());
                    log.debug("SNMP响应数据：oid:[{}]---value:[{}]", vb.getOid(), vb.getVariable());
                }
            }
        } catch (Exception e) {
            log.error("SNMP获取OID信息异常: oid:[{}]", oid, e);
        } finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ex1) {
                    snmp = null;
                }
            }

        }
        return values;
    }

    /**
     * 根据OID列表，一次获取多条OID数据，并且以List形式返回
     *
     * @param ip        设备IP
     * @param community 设备组织
     * @param oidList   参数数据标识列表
     */
    public static Map<String, Variable> snmpGetList(String ip, String community, List<String> oidList) {
        log.debug("SNMP获取参数信息：oidList=[{}]", oidList);
        ConcurrentHashMap<String, Variable> values = new ConcurrentHashMap<>(oidList.size());
        CommunityTarget target = createDefault(ip, community);
        Snmp snmp = null;
        try {
            PDU pdu = new PDU();
            for (String oid : oidList) {
                pdu.add(new VariableBinding(new OID(oid)));
            }
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            snmp.listen();
            pdu.setType(PDU.GET);
            ResponseEvent respEvent = snmp.send(pdu, target);
            log.debug("SNMP发送PDU到地址：[{}]---", respEvent.getPeerAddress());
            PDU response = respEvent.getResponse();

            if (response == null) {
                log.debug("response is null, request time out");
            } else {
                log.debug("response pdu size is " + response.size());
                for (int i = 0; i < response.size(); i++) {
                    VariableBinding vb = response.get(i);
                    values.put(vb.getOid().toString(), vb.getVariable());
                    log.debug("SNMP响应数据：oid:[{}]---value:[{}]", vb.getOid(), vb.getVariable());
                }
            }
        } catch (Exception e) {
            log.error("SNMP获取OID列表信息异常: oidList:[{}]", oidList, e);
        } finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ex1) {
                    snmp = null;
                }
            }
        }
        return values;
    }

    /**
     * 根据OID和指定string来设置设备的数据
     *
     * @param ip        设备IP
     * @param community 设备组织
     * @param val       需要设置的参数值
     */
    public static String setPDU(String ip, String community, VariableBinding val) {
        long timeStart = System.currentTimeMillis();
        CommunityTarget target = createDefault(ip, community);
        Snmp snmp = new Snmp();
        PDU pdu = new PDU();
        pdu.add(val);
        pdu.setType(PDU.SET);
        //默认成功
        String respCode = SUCCESS;
        try {
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            snmp.listen();
            log.info("发送SNMP设置帧到ip:[{}],参数oid:[{}]---参数值：[{}]", ip, val.getOid(), val);
            ResponseEvent responseEvent = snmp.send(pdu, target);
            PDU response = responseEvent.getResponse();
            if (response == null) {
                log.info("Control response is null, request time out");
                respCode = FAIL;
            } else {
                if (response.getErrorStatus() != 0) {
                    respCode = FAIL;
                }
                log.info("SNMP控制响应：错误码：[{}],错误信息：[{}]", response.getErrorStatus(), response.getErrorStatusText());
            }
        } catch (Exception e) {
            log.error("SNMP发送控制参数命令异常：oid=[{}],设置参数值：[{}]", val.getOid(), val, e);
        } finally {
            try {
                snmp.close();
            } catch (IOException ex1) {
            }
        }
        log.info("SNMP单次设置值耗时：[{}]", System.currentTimeMillis() - timeStart);
        return respCode;
    }

    /**
     * 根据OID和指定string来设置设备的数据
     * @param targetAddress TRAP地址
     * @param community TRAP团体
     * @param values TRAP消息
     * @throws IOException
     */
    public static void trapPDU(String targetAddress, String community, List<VariableBinding> values) throws IOException {
        //trap发送到接收端
        Address address = GenericAddress.parse(DEFAULT_PROTOCOL + ":" + targetAddress + "/" + DEFAULT_TRAP_PORT);
        CommunityTarget target = createDefault(targetAddress, community);
        target.setAddress(address);

        Snmp snmp;
        PDU pdu = new PDU();
        pdu.setVariableBindings(values);
        pdu.setType(PDU.TRAP);

        DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        log.debug("SNMP发送trap帧到ip:[{}],trap消息size:[{}]",address,values.size());
        snmp.send(pdu, target);
        snmp.close();
    }

    /**
     * 根据OID列表，采用异步方式一次获取多条OID数据，并且以List形式返回
     *
     * @param ip        设备IP
     * @param community 设备组织
     * @param oidList   参数数据标识列表
     */
    public static void snmpAsyncGetList(String ip, String community, List<String> oidList) {
        CommunityTarget target = createDefault(ip, community);
        Snmp snmp = null;
        try {
            PDU pdu = new PDU();
            for (String oid : oidList) {
                pdu.add(new VariableBinding(new OID(oid)));
            }
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            snmp.listen();
            pdu.setType(PDU.GET);
            ResponseEvent respEvent = snmp.send(pdu, target);
            log.debug("SNMP发送PDU到地址：[{}]---", respEvent.getPeerAddress());
            PDU response = respEvent.getResponse();

            /**异步获取*/
            final CountDownLatch latch = new CountDownLatch(1);
            ResponseListener listener = new ResponseListener() {
                public void onResponse(ResponseEvent event) {
                    ((Snmp) event.getSource()).cancel(event.getRequest(), this);
                    PDU response = event.getResponse();
                    PDU request = event.getRequest();
                    log.debug("[request]:" + request);
                    if (response == null) {
                        log.debug("[ERROR]: response is null");
                    } else if (response.getErrorStatus() != 0) {
                        log.debug("[ERROR]: response status"
                                + response.getErrorStatus() + " Text:"
                                + response.getErrorStatusText());
                    } else {
                        log.debug("Received response Success!");
                        for (int i = 0; i < response.size(); i++) {
                            VariableBinding vb = response.get(i);
                            log.debug(vb.getOid() + " = "
                                    + vb.getVariable());
                        }
                        log.debug("SNMP Asyn GetList OID finished. ");
                        latch.countDown();
                    }
                }
            };

            pdu.setType(PDU.GET);
            snmp.send(pdu, target, null, listener);
            log.debug("asyn send pdu wait for response...");

            boolean wait = latch.await(30, TimeUnit.SECONDS);
            log.debug("latch.await =:" + wait);

            snmp.close();

            log.debug("SNMP GET one OID value finished !");
        } catch (Exception e) {
            log.error("SNMP异步获取OID列表信息异常: oidList:[{}]", oidList, e);
        } finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ex1) {
                    snmp = null;
                }
            }

        }
    }

    /**
     * 根据targetOID，获取树形数据
     *
     * @param ip        设备IP
     * @param community 设备组织
     * @param targetOid 参数数据标识
     */
    public static void snmpWalk(String ip, String community, String targetOid) {
        CommunityTarget target = createDefault(ip, community);
        Snmp snmp = null;
        try {
            DefaultUdpTransportMapping transport;
            transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();

            PDU pdu = new PDU();
            OID targetOID = new OID(targetOid);
            pdu.add(new VariableBinding(targetOID));

            boolean finished = false;
            log.debug("SNMP开始Walk节点：[{}]...", targetOid);
            while (!finished) {
                VariableBinding vb;
                ResponseEvent respEvent = snmp.getNext(pdu, target);
                PDU response = respEvent.getResponse();
                if (null == response) {
                    log.debug("snmpWalk方法获取到response为空");
                    finished = true;
                    break;
                } else {
                    vb = response.get(0);
                }
                // check finish
                finished = checkWalkFinished(targetOID, pdu, vb);
                if (!finished) {
                    log.debug("==== walk each vlaue :");
                    log.debug(vb.getOid() + " = " + vb.getVariable());

                    // Set up the variable binding for the next entry.
                    pdu.setRequestID(new Integer32(0));
                    pdu.set(0, vb);
                } else {
                    log.debug("SNMP walk OID has finished.");
                    snmp.close();
                }
            }
            log.debug("----> demo end <----");
        } catch (Exception e) {
            log.error("SNMP遍历walk节点信息异常: oid:[{}]", targetOid, e);
        } finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ex1) {
                    snmp = null;
                }
            }
        }
    }

    private static boolean checkWalkFinished(OID targetOID, PDU pdu, VariableBinding vb) {
        boolean finished = false;
        if (pdu.getErrorStatus() != 0) {
            log.debug("[true] responsePDU.getErrorStatus() != 0 ");
            log.debug(pdu.getErrorStatusText());
            finished = true;
        } else if (vb.getOid() == null) {
            log.debug("[true] vb.getOid() == null");
            finished = true;
        } else if (vb.getOid().size() < targetOID.size()) {
            log.debug("[true] vb.getOid().size() < targetOID.size()");
            finished = true;
        } else if (targetOID.leftMostCompare(targetOID.size(), vb.getOid()) != 0) {
            log.debug("[true] targetOID.leftMostCompare() != 0");
            finished = true;
        } else if (Null.isExceptionSyntax(vb.getVariable().getSyntax())) {
            System.out
                    .println("[true] Null.isExceptionSyntax(vb.getVariable().getSyntax())");
            finished = true;
        } else if (vb.getOid().compareTo(targetOID) <= 0) {
            log.debug("[true] Variable received is not "
                    + "lexicographic successor of requested " + "one:");
            log.debug(vb.toString() + " <= " + targetOID);
            finished = true;
        }
        return finished;

    }

    /*根据targetOID，异步获取树形数据*/
    public static void snmpAsynWalk(String ip, String community, String oid) {
        final CommunityTarget target = createDefault(ip, community);
        Snmp snmp = null;
        try {
            log.debug("----> demo start <----");

            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            snmp.listen();

            final PDU pdu = new PDU();
            final OID targetOID = new OID(oid);
            final CountDownLatch latch = new CountDownLatch(1);
            pdu.add(new VariableBinding(targetOID));

            ResponseListener listener = new ResponseListener() {
                public void onResponse(ResponseEvent event) {
                    ((Snmp) event.getSource()).cancel(event.getRequest(), this);

                    try {
                        PDU response = event.getResponse();
                        // PDU request = event.getRequest();
                        // log.debug("[request]:" + request);
                        if (response == null) {
                            log.debug("[ERROR]: response is null");
                        } else if (response.getErrorStatus() != 0) {
                            log.debug("[ERROR]: response status"
                                    + response.getErrorStatus() + " Text:"
                                    + response.getErrorStatusText());
                        } else {
                            System.out
                                    .println("Received Walk response value :");
                            VariableBinding vb = response.get(0);

                            boolean finished = checkWalkFinished(targetOID,
                                    pdu, vb);
                            if (!finished) {
                                log.debug(vb.getOid() + " = "
                                        + vb.getVariable());
                                pdu.setRequestID(new Integer32(0));
                                pdu.set(0, vb);
                                ((Snmp) event.getSource()).getNext(pdu, target,
                                        null, this);
                            } else {
                                System.out
                                        .println("SNMP Asyn walk OID value success !");
                                latch.countDown();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        latch.countDown();
                    }
                }
            };

            snmp.getNext(pdu, target, null, listener);
            log.debug("pdu 已发送,等到异步处理结果...");

            boolean wait = latch.await(30, TimeUnit.SECONDS);
            log.debug("latch.await =:" + wait);
            snmp.close();

            log.debug("----> demo end <----");
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("SNMP Asyn Walk Exception:" + e);
        }
    }


    public static void main(String[] args) {
//        String ip = "192.168.7.31";
        String ip = "127.0.0.1";
        String community = "public";
//        String oid = ".1.3.6.1.4.1.589.10.2.1.3.0";
        String localOid = ".1.3.6.1.2.1.4.20.1.4.127.0.0.1";
//        String oidval1 = "1.3.6.1.4.1.589.10.2.1.4";
        Map<String, Variable> map = SnmpUtil.snmpGet(ip, community, localOid);
        log.debug("map:{}", map);
//        SnmpUtil.setPDU(ip, community, localOid,);

    }
}