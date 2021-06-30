package com.xy.netdev.synthetical;

import com.xy.netdev.synthetical.service.impl.OidAccessService;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * SNMP消息接收和处理
 */
@Component
@Slf4j
public class SnmpMsgRecvHandler implements CommandResponder, ApplicationRunner {

    @Autowired
    private SnmpAddressConfig appConfigRead;
    @Autowired
    private OidAccessService oidAccessService;

    private MultiThreadedMessageDispatcher dispatcher;
    private Snmp snmp = null;
    private Address listenAddress;
    private Address targetAddress;
    private ThreadPool threadPool;
    private SimpleDateFormat simpleDateFormat;
    private String port;

    public SnmpMsgRecvHandler() {
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    private void init() throws IOException {
        TransportMapping transport;
        threadPool = ThreadPool.create("SNMP-RECEIVE-HANDLER", 2);
        dispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());
        listenAddress = GenericAddress.parse(System.getProperty("snmp4j.listenAddress", "udp:" + appConfigRead.getListenAddress())); // 本地IP与监听端口
        // 对TCP与UDP协议进行处理
        if (listenAddress instanceof UdpAddress) {
            transport = new DefaultUdpTransportMapping((UdpAddress) listenAddress);
            log.info("使用UDP协议");
        } else {
            transport = new DefaultTcpTransportMapping((TcpAddress) listenAddress);
            log.info("使用TCP协议");
        }
        snmp = new Snmp(dispatcher, transport);
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
//        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());
//        USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
//        SecurityModels.getInstance().addSecurityModel(usm);
        snmp.listen();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            init();
            snmp.addCommandResponder(this);
            log.info("开始监听网络规划SNMP请求信息,监听地址：[{}]", listenAddress.toString());
        } catch (Exception ex) {
            log.error("监听异常");
            ex.printStackTrace();
        }
    }

    /**
     * 发送响应PDU
     *
     * @param respEvent SNMP监听事件
     * @param bindings  SNMP参数值
     */
    private void dispatchRespPdu(CommandResponderEvent respEvent, List<VariableBinding> bindings) {
        port = respEvent.getPeerAddress().toString().split("/")[1];
        // 设置 target
        CommunityTarget target = new CommunityTarget();
        targetAddress = GenericAddress.parse(System.getProperty(
                "snmp4j.listenAddress", "udp:" + appConfigRead.getTargetAddress() + "/" + port)); // 本地IP与监听端口
        target.setAddress(targetAddress);
        target.setCommunity(new OctetString("public"));
        target.setVersion(SnmpConstants.version2c);

        ResponseEvent send;

        PDU sendPdu = new PDU();
        sendPdu.setErrorStatus(0);
        sendPdu.setRequestID(respEvent.getPDU().getRequestID());
        sendPdu.setVariableBindings(bindings);
        sendPdu.setType(PDU.RESPONSE);
        try {
//            try {
//                Thread.sleep(30);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            send = snmp.send(sendPdu, target);
            log.debug("SNMP发送接收响应信息,发送地址：[{}],发送内容：[{}],发送结果：[{}]", target.getAddress().toString(), sendPdu, send);
        } catch (IOException e) {
            log.error("SNMP相应信息发送失败！");
        }
    }

    @Override
    public void processPdu(CommandResponderEvent respEvent) {
        // 解析Response
        if (respEvent != null && respEvent.getPDU() != null) {
            log.debug("接收到SNMP消息帧：event信息:[{}]", respEvent);
            PDU pdu = respEvent.getPDU();
            int type = pdu.getType();
            Vector<? extends VariableBinding> variables = pdu.getVariableBindings();
            List<OID> oidList = variables.stream().map(VariableBinding::getOid).collect(Collectors.toList());

            List<VariableBinding> targetVariables = new ArrayList<>();
            if (PDU.GET == type || PDU.GETNEXT == type) {
                //获取到需要查询的所有OID
                List<String> oidStrings = oidList.stream().map(OID::toString).collect(Collectors.toList());
                targetVariables = oidAccessService.getVariablesByOidList(oidStrings);
            } else if (PDU.SET == type) {
            }

            dispatchRespPdu(respEvent, targetVariables);

//            doTest(respEvent, oidList);
        }
    }

    private void doTest(CommandResponderEvent respEvent, List<OID> oidList) {
        String oid1 = oidList.get(0).toString();

        if (oid1.contains("145.3")) {
            //TODO 模拟响应结果发送
            ArrayList<VariableBinding> bindings = new ArrayList<>();
            VariableBinding variableBinding1 = new VariableBinding();
            variableBinding1.setOid(new OID("1.3.6.1.4.1.63000.2.2.2.145.3.1.1.4"));
            variableBinding1.setVariable(new Integer32(1));

            VariableBinding variableBinding2 = new VariableBinding();
            variableBinding2.setOid(new OID("1.3.6.1.4.1.63000.2.2.2.145.3.1.1.11"));
            variableBinding2.setVariable(new Integer32(128));

            VariableBinding variableBinding3 = new VariableBinding();
            variableBinding3.setOid(new OID("1.3.6.1.4.1.63000.2.2.2.145.3.1.1.12"));
            variableBinding3.setVariable(new Integer32(128));

            VariableBinding variableBinding4 = new VariableBinding();
            variableBinding4.setOid(new OID("1.3.6.1.4.1.63000.2.2.2.145.3.1.1.13"));
            variableBinding4.setVariable(new Integer32(128));

            bindings.add(variableBinding1);
            bindings.add(variableBinding2);
            bindings.add(variableBinding3);
            bindings.add(variableBinding4);

//            ArrayList<VariableBinding> bindings = new ArrayList<>();
//            VariableBinding variableBinding1 = new VariableBinding();
//            variableBinding1.setOid(new OID("1.3.6.1.4.1.63000.2.2.2.119.109.1.1.4.1.1.1"));
//            variableBinding1.setVariable(new Integer32(1));
//
//            bindings.add(variableBinding1);

            dispatchRespPdu(respEvent, bindings);
        }
    }
}
