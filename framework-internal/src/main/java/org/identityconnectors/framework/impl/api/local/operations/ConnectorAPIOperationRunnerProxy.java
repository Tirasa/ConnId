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
package org.identityconnectors.framework.impl.api.local.operations;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.impl.api.local.ObjectPool;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.PoolableConnector;


/**
 * Proxy for APIOperationRunner that takes care of setting up underlying
 * connector and creating the implementation of APIOperationRunner.
 * The implementation of APIOperationRunner gets created whenever the
 * actual method is invoked.
 */
public class ConnectorAPIOperationRunnerProxy implements InvocationHandler {

    private static final Log _log = Log.getLog(ConnectorAPIOperationRunnerProxy.class);
    
    /**
     * The operational context
     */
    private final ConnectorOperationalContext _context;
    
    /**
     * The implementation constructor. The instance is lazily created upon
     * invocation
     */
    private final Constructor<? extends APIOperationRunner> _runnerImplConstructor;
    
    /**
     * Create an APIOperationRunnerProxy
     * @param context The operational context
     * @param runnerImplConstructor The implementation constructor. Implementation
     * must define a two-argument constructor(OperationalContext,Connector)
     */
    public ConnectorAPIOperationRunnerProxy(ConnectorOperationalContext context,
            Constructor<? extends APIOperationRunner> runnerImplConstructor) {
        _context = context;
        _runnerImplConstructor = runnerImplConstructor;
    }
    
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        //do not proxy equals, hashCode, toString
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        Object ret = null;
        Connector connector = null;
        ObjectPool<PoolableConnector> pool = _context.getPool();
        // get the connector class..
        Class<? extends Connector> connectorClazz = _context.getConnectorClass();
        try {
            // pooling is implemented get one..
            if (pool != null) {
                connector = pool.borrowObject();
            }
            else {
                // get a new instance of the connector..
                connector = connectorClazz.newInstance();
                // initialize the connector..
                connector.init(_context.getConfiguration());
            }
            APIOperationRunner runner = 
                _runnerImplConstructor.newInstance(_context,connector);
            ret = method.invoke(runner, args);
            // call out to the operation..
        } catch (InvocationTargetException e) {
            Throwable root = e.getCause();
            throw root;
        } finally {
            
            // make sure dispose of the connector properly
            if (connector != null) {
                // determine if there was a pool..
                if (pool != null) {
                    try {
                        //try to return it to the pool even though an
                        //exception may have happened that leaves it in
                        //a bad state. The contract of checkAlive
                        //is that it will tell you if the connector is
                        //still valid and so we leave it up to the pool
                        //and connector to work it out.
                        pool.returnObject((PoolableConnector)connector);
                    } catch (Exception e) {
                        //don't let pool exceptions propogate or mask
                        //other exceptions. do log it though.
                        _log.error(e, null);
                    }
                }
                //not pooled - just dispose
                else {
                    //dispose it not supposed to throw, but just in case,
                    //catch the exception and log it so we know about it
                    //but don't let the exception prevent additional
                    //cleanup that needs to happen
                    try {
                        connector.dispose();
                    } catch (Exception e) {
                        //log this though
                        _log.error(e, null);
                    }                    
                }
            }
        }
        return ret;
    }
    


}
