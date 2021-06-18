package com.xy.netdev.synthetical.agent;

import com.xy.netdev.admin.service.ISysParamService;
import com.xy.netdev.common.constant.SysConfigConstant;
import com.xy.netdev.container.DevParaInfoContainer;
import com.xy.netdev.synthetical.bo.OidParaInfo;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.DefaultMOServer;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.MOServer;
import org.snmp4j.agent.cfg.EngineBootsCounterFile;
import org.snmp4j.agent.example.Modules;
import org.snmp4j.agent.example.SampleAgent;
import org.snmp4j.agent.io.DefaultMOPersistenceProvider;
import org.snmp4j.agent.io.MOInput;
import org.snmp4j.agent.io.MOInputFactory;
import org.snmp4j.agent.io.prop.PropertyMOInput;
import org.snmp4j.agent.mo.*;
import org.snmp4j.log.ConsoleLogFactory;
import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;
import org.snmp4j.log.LogLevel;
import org.snmp4j.mp.MPv3;
import org.snmp4j.smi.*;
import org.snmp4j.transport.TransportMappings;
import org.snmp4j.util.ThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.Properties;

/**
 * <p>
 * snmp agent
 * </p>
 *
 * @author tangxl
 * @since 2021-06-17
 */
@Component
public class XyAgent implements ApplicationRunner {

    @Autowired
    private ISysParamService sysParamService;


    static {
        LogFactory.setLogFactory(new ConsoleLogFactory());
        LogFactory.getLogFactory().getRootLogger().setLogLevel(LogLevel.ALL);
    }

    private static final LogAdapter logger = LogFactory.getLogger(XyAgent.class);

    protected XyAgentConfigManager agent;
    protected MOServer server;
    private String configFile;
    private File bootCounterFile;

    // supported MIBs
    protected Modules modules;

    protected Properties tableSizeLimits;

    @SuppressWarnings("unchecked")
    public XyAgent() { }

    private void init(){
        configFile = "E://netdev//config.data";
        bootCounterFile = new File("E://netdev//bootCounter.data");

        server = new DefaultMOServer();
        MOServer[] moServers = new MOServer[]{server};
        InputStream configInputStream =SampleAgent.class.getResourceAsStream("SampleAgentConfig.properties");
        final Properties props = new Properties();
        try {
            props.load(configInputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        MOInputFactory configurationFactory = new MOInputFactory() {
            public MOInput createMOInput() {
                return new PropertyMOInput(props, agent);
            }
        };
        InputStream tableSizeLimitsInputStream =SampleAgent.class.getResourceAsStream("SampleAgentTableSizeLimits.properties");
        tableSizeLimits = new Properties();
        try {
            tableSizeLimits.load(tableSizeLimitsInputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        MessageDispatcher messageDispatcher = new MessageDispatcherImpl();
        addListenAddresses(messageDispatcher);
        agent = new XyAgentConfigManager(new OctetString(MPv3.createLocalEngineID()),
                messageDispatcher,
                null,
                moServers,
                ThreadPool.create("XyAgent", 3),
                configurationFactory,
                new DefaultMOPersistenceProvider(moServers,configFile),
                new EngineBootsCounterFile(bootCounterFile));
    }

    protected void addListenAddresses(MessageDispatcher md) {
            String   addressString =sysParamService.getParaRemark1(SysConfigConstant.AGENT_IP_ADDRESS);
            Address address = GenericAddress.parse(addressString);
            if (address == null) {
                logger.fatal("Could not parse address string '" + addressString + "'");
                return;
            }
            TransportMapping<? extends Address> tm =
                    TransportMappings.getInstance().createTransportMapping(address);
            if (tm != null) {
                md.addTransportMapping(tm);
            } else {
                logger.warn("No transport mapping available for address '" +
                        address + "'.");
            }
    }
    /**
     * Get the {@link MOFactory} that creates the various MOs (MIB Objects).
     *
     * @return a {@link DefaultMOFactory} instance by default.
     * @since 1.3.2
     */
    protected MOFactory getFactory() {
        return DefaultMOFactory.getInstance();
    }

    /**
     * Register your own MIB modules in the specified context of the agent.
     * The {@link MOFactory} provided to the {@code Modules} constructor
     * is returned by {@link #getFactory()}.
     */
    protected void registerMIBs() {
        DevParaInfoContainer.getDevParaOidMap().keySet().forEach(paraOid -> {
            try {
                server.register(new OidParaInfo(paraOid), null);
            } catch (DuplicateRegistrationException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // initialize agent before registering our own modules
        init();
        agent.initialize();
        // switch logging of notifications to log sent notifications instead
        // of logging the original internal notification event:
        //agent.getNotificationLogMIB().setLoggerMode(
        //  NotificationLogMib.Snmp4jNotificationLogModeEnum.sent);
        // this requires sysUpTime to be available.
        // add proxy forwarder
        agent.setupProxyForwarder();
        registerMIBs();
        // apply table size limits
        agent.setTableSizeLimits(tableSizeLimits);
        // register shutdown hook to be able to automatically commit configuration to persistent storage
        agent.registerShutdownHook();
        // now continue agent setup and launch it.
        agent.run();
    }
}

