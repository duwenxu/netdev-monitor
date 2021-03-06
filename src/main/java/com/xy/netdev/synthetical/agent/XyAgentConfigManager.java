package com.xy.netdev.synthetical.agent;

import org.snmp4j.MessageDispatcher;
import org.snmp4j.agent.*;
import org.snmp4j.agent.cfg.EngineBootsProvider;
import org.snmp4j.agent.io.MOInputFactory;
import org.snmp4j.agent.io.MOPersistenceProvider;
import org.snmp4j.agent.mo.util.VariableProvider;
import org.snmp4j.agent.request.Request;
import org.snmp4j.agent.request.RequestStatus;
import org.snmp4j.agent.request.SubRequest;
import org.snmp4j.agent.request.SubRequestIterator;
import org.snmp4j.agent.security.VACM;
import org.snmp4j.mp.MPv3;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.util.WorkerPool;


public class XyAgentConfigManager extends AgentConfigManager implements VariableProvider {
    public XyAgentConfigManager(OctetString agentsOwnEngineID,
                              MessageDispatcher messageDispatcher,
                              VACM vacm,
                              MOServer[] moServers,
                              WorkerPool workerPool,
                              MOInputFactory configurationFactory,
                              MOPersistenceProvider persistenceProvider,
                              EngineBootsProvider engineBootsProvider) {
        super(agentsOwnEngineID,
                messageDispatcher,
                vacm,
                moServers,
                workerPool,
                configurationFactory,
                persistenceProvider,
                engineBootsProvider);
    }

    public Variable getVariable(String name) {
        OID oid;
        OctetString context = null;
        int pos = name.indexOf(':');
        if (pos >= 0) {
            context = new OctetString(name.substring(0, pos));
            oid = new OID(name.substring(pos + 1, name.length()));
        } else {
            oid = new OID(name);
        }
        MOServer server = agent.getServer(context);
        final DefaultMOContextScope scope =
                new DefaultMOContextScope(context, oid, true, oid, true);
        MOQuery query = new MOQueryWithSource(scope, false, this);
        ManagedObject mo = server.lookup(query);
        if (mo != null) {
            final VariableBinding vb = new VariableBinding(oid);
            final RequestStatus status = new RequestStatus();
            SubRequest req = new SubRequest() {
                private boolean completed;
                private MOQuery query;

                public boolean hasError() {
                    return false;
                }

                public void setErrorStatus(int errorStatus) {
                    status.setErrorStatus(errorStatus);
                }

                public int getErrorStatus() {
                    return status.getErrorStatus();
                }

                public RequestStatus getStatus() {
                    return status;
                }

                public MOScope getScope() {
                    return scope;
                }

                public VariableBinding getVariableBinding() {
                    return vb;
                }

                public Request getRequest() {
                    return null;
                }

                public Object getUndoValue() {
                    return null;
                }

                public void setUndoValue(Object undoInformation) {
                }

                public void completed() {
                    completed = true;
                }

                public boolean isComplete() {
                    return completed;
                }

                public void setTargetMO(ManagedObject managedObject) {
                }

                public ManagedObject getTargetMO() {
                    return null;
                }

                public int getIndex() {
                    return 0;
                }

                public void setQuery(MOQuery query) {
                    this.query = query;
                }

                public MOQuery getQuery() {
                    return query;
                }

                public SubRequestIterator<SubRequest> repetitions() {
                    return null;
                }

                public void updateNextRepetition() {
                }

                public Object getUserObject() {
                    return null;
                }

                public void setUserObject(Object userObject) {
                }

            };
            mo.get(req);
            return vb.getVariable();
        }
        return null;
    }

    /**
     * Fire notifications after agent start, i.e. sending a coldStart trap.
     */
    protected void fireLaunchNotifications() {

    }

    protected void registerMIBs(OctetString context) throws
            DuplicateRegistrationException
    {
        MOServer server = agent.getServer(context);
        targetMIB.registerMOs(server, getContext(targetMIB, context));
       // notificationMIB.registerMOs(server, getContext(notificationMIB, context));
        vacmMIB.registerMOs(server, getContext(vacmMIB, context));
       // usmMIB.registerMOs(server, getContext(usmMIB, context));
       // snmpv2MIB.registerMOs(server, getContext(snmpv2MIB, context));
       // frameworkMIB.registerMOs(server, getContext(frameworkMIB, context));
       communityMIB.registerMOs(server, getContext(communityMIB, context));

    }

    /**
     * Unregister the initialized MIB modules from the default context of the
     * agent.
     * @param context
     *    the context where the MIB modules have been previously registered.
     */
    protected void unregisterMIBs(OctetString context) {
        MOServer server = agent.getServer(context);
        targetMIB.unregisterMOs(server, getContext(targetMIB, context));
       // notificationMIB.unregisterMOs(server, getContext(notificationMIB, context));
        vacmMIB.unregisterMOs(server, getContext(vacmMIB, context));
       // usmMIB.unregisterMOs(server, getContext(usmMIB, context));
       // snmpv2MIB.unregisterMOs(server, getContext(snmpv2MIB, context));
       //frameworkMIB.unregisterMOs(server, getContext(frameworkMIB, context));
       communityMIB.unregisterMOs(server, getContext(communityMIB, context));
    }
}
