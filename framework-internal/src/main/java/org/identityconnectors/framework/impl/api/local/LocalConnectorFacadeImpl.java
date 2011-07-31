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
 * http://IdentityConnectors.dev.java.net/legal/license.txt
 * See the License for the specific language governing permissions and limitations 
 * under the License. 
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.framework.impl.api.local;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.AuthenticationApiOp;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.ResolveUsernameApiOp;
import org.identityconnectors.framework.api.operations.SchemaApiOp;
import org.identityconnectors.framework.api.operations.ScriptOnConnectorApiOp;
import org.identityconnectors.framework.api.operations.ScriptOnResourceApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.api.operations.SyncApiOp;
import org.identityconnectors.framework.api.operations.TestApiOp;
import org.identityconnectors.framework.api.operations.UpdateApiOp;
import org.identityconnectors.framework.api.operations.ValidateApiOp;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.AbstractConnectorFacade;
import org.identityconnectors.framework.impl.api.local.operations.APIOperationRunner;
import org.identityconnectors.framework.impl.api.local.operations.AuthenticationImpl;
import org.identityconnectors.framework.impl.api.local.operations.ConnectorAPIOperationRunner;
import org.identityconnectors.framework.impl.api.local.operations.ConnectorAPIOperationRunnerProxy;
import org.identityconnectors.framework.impl.api.local.operations.ConnectorOperationalContext;
import org.identityconnectors.framework.impl.api.local.operations.CreateImpl;
import org.identityconnectors.framework.impl.api.local.operations.DeleteImpl;
import org.identityconnectors.framework.impl.api.local.operations.GetImpl;
import org.identityconnectors.framework.impl.api.local.operations.OperationalContext;
import org.identityconnectors.framework.impl.api.local.operations.ResolveUsernameImpl;
import org.identityconnectors.framework.impl.api.local.operations.SchemaImpl;
import org.identityconnectors.framework.impl.api.local.operations.ScriptOnConnectorImpl;
import org.identityconnectors.framework.impl.api.local.operations.ScriptOnResourceImpl;
import org.identityconnectors.framework.impl.api.local.operations.SearchImpl;
import org.identityconnectors.framework.impl.api.local.operations.SyncImpl;
import org.identityconnectors.framework.impl.api.local.operations.TestImpl;
import org.identityconnectors.framework.impl.api.local.operations.ThreadClassLoaderManagerProxy;
import org.identityconnectors.framework.impl.api.local.operations.UpdateImpl;
import org.identityconnectors.framework.impl.api.local.operations.ValidateImpl;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.PoolableConnector;


/**
 * Implements all the methods of the facade.
 * <p>  
 */
public class LocalConnectorFacadeImpl extends AbstractConnectorFacade {

    // =======================================================================
    // Constants
    // =======================================================================
    /**
     * Map the API interfaces to their implementation counterparts.
     */
    private static final Map<Class<? extends APIOperation>, Constructor<? extends ConnectorAPIOperationRunner>> API_TO_IMPL=
        new HashMap<Class<? extends APIOperation>, Constructor<? extends ConnectorAPIOperationRunner>>();

    private static void addImplementation(Class<? extends APIOperation> inter,
            Class<? extends ConnectorAPIOperationRunner> impl) {
        Constructor<? extends ConnectorAPIOperationRunner> constructor;
        try {
            constructor = impl.getConstructor(ConnectorOperationalContext.class,Connector.class);
            API_TO_IMPL.put(inter, constructor);
        }
        catch (Exception e) {
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
        addImplementation(AuthenticationApiOp.class, AuthenticationImpl.class);
        addImplementation(ResolveUsernameApiOp.class, ResolveUsernameImpl.class);
        addImplementation(TestApiOp.class, TestImpl.class);
        addImplementation(ScriptOnConnectorApiOp.class, ScriptOnConnectorImpl.class);
        addImplementation(ScriptOnResourceApiOp.class, ScriptOnResourceImpl.class);
        addImplementation(SyncApiOp.class, SyncImpl.class);
    }

    // =======================================================================
    // Fields
    // =======================================================================
    
    /**
     * The connector info
     */
    private final LocalConnectorInfoImpl connectorInfo;

    /**
     * Builds up the maps of supported operations and calls.
     */
    public LocalConnectorFacadeImpl(final LocalConnectorInfoImpl connectorInfo,
            final APIConfigurationImpl apiConfiguration)  {
        super(apiConfiguration);
        this.connectorInfo = connectorInfo;
    }

    
    // =======================================================================
    // ConnectorFacade Interface
    // =======================================================================

    @Override
    protected APIOperation getOperationImplementation(final Class<? extends APIOperation> api) {

                
        APIOperation proxy;
        //first create the inner proxy - this is the proxy that obtaining
        //a connector from the pool, etc
        //NOTE: we want to skip this part of the proxy for
        //validate op, but we will want the timeout proxy
        if ( api == ValidateApiOp.class ) {
            OperationalContext context =
                new OperationalContext(connectorInfo,getAPIConfiguration());
            proxy = new ValidateImpl(context);
        }
        else if ( api == GetApiOp.class ) {
            Constructor<? extends APIOperationRunner> constructor =
                API_TO_IMPL.get(SearchApiOp.class);
            ConnectorOperationalContext context =
            new ConnectorOperationalContext(connectorInfo,
                    getAPIConfiguration(), 
                    getPool());
        
            ConnectorAPIOperationRunnerProxy handler =
                new ConnectorAPIOperationRunnerProxy(context,constructor);
            proxy = 
                new GetImpl((SearchApiOp)newAPIOperationProxy(SearchApiOp.class, handler));            
        }
        else {
            Constructor<? extends APIOperationRunner> constructor =
                API_TO_IMPL.get(api);
            ConnectorOperationalContext context =
            new ConnectorOperationalContext(connectorInfo,
                    getAPIConfiguration(), 
                    getPool());
        
            ConnectorAPIOperationRunnerProxy handler =
                new ConnectorAPIOperationRunnerProxy(context,constructor);
            proxy = newAPIOperationProxy(api, handler);
        }
        
        //now proxy to setup the thread-local classloader
        proxy = newAPIOperationProxy(api, 
                new ThreadClassLoaderManagerProxy(
                        connectorInfo.getConnectorClass().getClassLoader(),
                        proxy));
        
        //now wrap the proxy in the appropriate timeout proxy
        proxy = createTimeoutProxy(api, proxy);
        // wrap in a logging proxy..
        proxy = createLoggingProxy(api, proxy);
        return proxy;
    }

    
    private ObjectPool<PoolableConnector> getPool()
    {
        return ConnectorPoolManager.getPool(getAPIConfiguration(),connectorInfo);        
    }

}
