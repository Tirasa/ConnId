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

import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.local.ObjectPool;
import org.identityconnectors.framework.impl.api.local.LocalConnectorInfoImpl;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.PoolableConnector;


/**
 * Simple structure to pass more variables through the constructor of
 * {@link APIOperationRunner}.
 */
public class ConnectorOperationalContext extends OperationalContext {
    
    
    /**
     * Pool for Connectors
     */
    private final ObjectPool<PoolableConnector> pool;
    

    public ConnectorOperationalContext(final LocalConnectorInfoImpl connectorInfo,
            final APIConfigurationImpl apiConfiguration,
            final ObjectPool<PoolableConnector> pool) {
        super(connectorInfo,apiConfiguration);
        this.pool = pool;
    }

    public ObjectPool<PoolableConnector> getPool() {
        return this.pool;
    }


    public Class<? extends Connector> getConnectorClass() {
        return getConnectorInfo().getConnectorClass();
    }

}
