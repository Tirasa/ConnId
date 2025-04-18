/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://opensource.org/licenses/cddl1.php
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://opensource.org/licenses/cddl1.php.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 * Portions Copyrighted 2010-2014 ForgeRock AS.
 * Portions Copyrighted 2024 ConnId
 */
package org.identityconnectors.framework.impl.api.local;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import org.identityconnectors.framework.api.operations.*;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.AbstractConnectorFacade;
import org.identityconnectors.framework.impl.api.LoggingProxy;
import org.identityconnectors.framework.impl.api.local.operations.*;
import org.identityconnectors.framework.spi.Connector;

/**
 * Implements all the methods of the facade.
 */
public class LocalConnectorFacadeImpl extends AbstractConnectorFacade {

    // =======================================================================
    // Constants
    // =======================================================================
    /**
     * Map the API interfaces to their implementation counterparts.
     */
    private static final Map<
            Class<? extends APIOperation>, Constructor<? extends ConnectorAPIOperationRunner>> API_TO_IMPL =
            new HashMap<>();

    private static void addImplementation(
            final Class<? extends APIOperation> inter,
            final Class<? extends ConnectorAPIOperationRunner> impl) {

        Constructor<? extends ConnectorAPIOperationRunner> constructor;
        try {
            constructor = impl.getConstructor(ConnectorOperationalContext.class, Connector.class);
            API_TO_IMPL.put(inter, constructor);
        } catch (Exception e) {
            // this should never happen..
            throw ConnectorException.wrap(e);
        }
    }

    static {
        addImplementation(CreateApiOp.class, CreateImpl.class);
        addImplementation(DeleteApiOp.class, DeleteImpl.class);
        addImplementation(SchemaApiOp.class, SchemaImpl.class);
        addImplementation(SearchApiOp.class, SearchImpl.class);
        addImplementation(UpdateApiOp.class, UpdateImpl.class);
        addImplementation(UpdateDeltaApiOp.class, UpdateDeltaImpl.class);
        addImplementation(AuthenticationApiOp.class, AuthenticationImpl.class);
        addImplementation(ResolveUsernameApiOp.class, ResolveUsernameImpl.class);
        addImplementation(TestApiOp.class, TestImpl.class);
        addImplementation(ScriptOnConnectorApiOp.class, ScriptOnConnectorImpl.class);
        addImplementation(ScriptOnResourceApiOp.class, ScriptOnResourceImpl.class);
        addImplementation(SyncApiOp.class, SyncImpl.class);
        addImplementation(LiveSyncApiOp.class, LiveSyncImpl.class);
        addImplementation(DiscoverConfigurationApiOp.class, DiscoverConfigurationImpl.class);
    }

    // =======================================================================
    // Fields
    // =======================================================================
    /**
     * The connector info
     */
    private final LocalConnectorInfoImpl connectorInfo;

    /**
     * Shared OperationalContext for stateful facades
     */
    private final ConnectorOperationalContext operationalContext;

    /**
     * Builds up the maps of supported operations and calls.
     */
    public LocalConnectorFacadeImpl(
            final LocalConnectorInfoImpl connectorInfo,
            final APIConfigurationImpl apiConfiguration) {

        super(apiConfiguration);
        this.connectorInfo = connectorInfo;
        if (connectorInfo.isConfigurationStateless() && !connectorInfo.isConnectorPoolingSupported()) {
            operationalContext = null;
        } else {
            operationalContext = new ConnectorOperationalContext(connectorInfo, getAPIConfiguration());
        }
    }

    public LocalConnectorFacadeImpl(final LocalConnectorInfoImpl connectorInfo, String configuration) {
        super(configuration, connectorInfo);
        this.connectorInfo = connectorInfo;
        if (connectorInfo.isConfigurationStateless() && !connectorInfo.isConnectorPoolingSupported()) {
            operationalContext = null;
        } else {
            operationalContext = new ConnectorOperationalContext(connectorInfo, getAPIConfiguration());
        }
    }

    @Override
    public void dispose() {
        if (null != operationalContext) {
            operationalContext.dispose();
        }
    }

    protected ConnectorOperationalContext getOperationalContext() {
        if (null == operationalContext) {
            return new ConnectorOperationalContext(connectorInfo, getAPIConfiguration());
        }
        return operationalContext;
    }

    // =======================================================================
    // ConnectorFacade Interface
    // =======================================================================
    @Override
    protected APIOperation getOperationImplementation(final Class<? extends APIOperation> api) {
        APIOperation proxy;
        // first create the inner proxy - this is the proxy that obtaining a connector from the pool, etc
        // NOTE: we want to skip this part of the proxy for validate op, but we will want the timeout proxy
        if (api == ValidateApiOp.class) {
            final OperationalContext context = new OperationalContext(connectorInfo, getAPIConfiguration());
            proxy = new ValidateImpl(context);
        } else if (api == GetApiOp.class) {
            final Constructor<? extends APIOperationRunner> constructor = API_TO_IMPL.get(SearchApiOp.class);
            final ConnectorAPIOperationRunnerProxy handler =
                    new ConnectorAPIOperationRunnerProxy(getOperationalContext(), constructor);
            proxy = new GetImpl((SearchApiOp) newAPIOperationProxy(SearchApiOp.class, handler));
        } else {
            final Constructor<? extends APIOperationRunner> constructor = API_TO_IMPL.get(api);
            final ConnectorAPIOperationRunnerProxy handler =
                    new ConnectorAPIOperationRunnerProxy(getOperationalContext(), constructor);
            proxy = newAPIOperationProxy(api, handler);
        }

        // now proxy to setup the thread-local classloader
        proxy = newAPIOperationProxy(api, new ThreadClassLoaderManagerProxy(
                connectorInfo.getConnectorClass().getClassLoader(), proxy));

        // now wrap the proxy in the appropriate timeout proxy
        proxy = createTimeoutProxy(api, proxy);
        // wrap in a logging proxy..
        if (LoggingProxy.isLoggable()) {
            proxy = createLoggingProxy(api, proxy);
        }
        return proxy;
    }
}
