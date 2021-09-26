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

    private Snmp snmp = null;
    private Address listenAddress;

    public SnmpMsgRecvHandler() {
    }

    private void init() throws IOException {
        TransportMapping transport;
        ThreadPool threadPool = ThreadPool.create("SNMP-RECEIVE-HANDLER", 2);
        MultiThreadedMessageDispatcher dispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());
        // 本地IP与监听端口
        listenAddress = GenericAddress.parse(System.getProperty("snmp4j.listenAddress", "udp:" + appConfigRead.getListenAddress()));
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
            log.error("监听异常",ex);
        }
    }

    /**
     * 发送响应PDU
     *
     * @param respEvent SNMP监听事件
     * @param bindings  SNMP参数值
     */
    private void dispatchRespPdu(CommandResponderEvent respEvent, List<VariableBinding> bindings) {
        //获取发送端端口作为发出的端口
        String port = respEvent.getPeerAddress().toString().split("/")[1];
        // 设置 target
        CommunityTarget target = new CommunityTarget();
        // 本地IP与监听端口
        Address targetAddress = GenericAddress.parse(System.getProperty(
                "snmp4j.listenAddress", "udp:" + appConfigRead.getTargetAddress() + "/" + port));
        target.setAddress(targetAddress);
        target.setCommunity(new OctetString("public"));
        target.setVersion(SnmpConstants.version2c);

        ResponseEvent send;

        //组装发送PDU
        PDU sendPdu = new PDU();
        /**设置错误状态 0：success*/
        sendPdu.setErrorStatus(0);
        /**设置请求ID*/
        sendPdu.setRequestID(respEvent.getPDU().getRequestID());
        /**设置值*/
        sendPdu.setVariableBindings(bindings);
        /**设置数据类型为 响应*/
        sendPdu.setType(PDU.RESPONSE);
        try {
            send = snmp.send(sendPdu, target);
            log.debug("SNMP发送接收响应信息,发送地址：[{}],发送内容：[{}],发送结果：[{}]", target.getAddress().toString(), sendPdu, send);
        } catch (IOException e) {
            log.error("SNMP相应信息发送失败！",e);
        }
    }

    /**
     * 处理监听到的流入的 SNMP请求、上报或通知消息
     * @param respEvent
     */
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
                //通过OID列表查询需要上报的参数值信息
                targetVariables = oidAccessService.getVariablesByOidList(oidStrings);
            } 
            //发送响应数据
            dispatchRespPdu(respEvent, targetVariables);
//            doTest(respEvent, oidList);
        }
    }

    /**
     * 网络规划软件上报测试
     * @param respEvent SNMP请求信息监听器
     * @param oidList 请求的OID参数列表
     */
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
