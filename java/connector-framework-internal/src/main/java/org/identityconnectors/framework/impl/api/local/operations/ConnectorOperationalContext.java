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
 * Portions Copyrighted 2010-2013 ForgeRock AS.
 */
package org.identityconnectors.framework.impl.api.local.operations;

import org.identityconnectors.common.Pair;
import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.local.ConnectorPoolManager;
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
     * Pool Key for Connectors
     */
    private ConnectorPoolManager.ConnectorPoolKey connectorPoolKey;

    public ConnectorOperationalContext(final LocalConnectorInfoImpl connectorInfo,
            final APIConfigurationImpl apiConfiguration) {
        super(connectorInfo, apiConfiguration);
    }

    public ObjectPool<PoolableConnector> getPool() {
        if (apiConfiguration.isConnectorPoolingSupported()) {
            if (null == connectorPoolKey) {
                Pair<ConnectorPoolManager.ConnectorPoolKey, ObjectPool<PoolableConnector>> pool =
                        ConnectorPoolManager.getPool(apiConfiguration, connectorInfo);

                connectorPoolKey = pool.getKey();
                return pool.getValue();
            } else {
                ObjectPool<PoolableConnector> pool = ConnectorPoolManager.getPool(connectorPoolKey);
                if (null == pool) {
                    //
                    Pair<ConnectorPoolManager.ConnectorPoolKey, ObjectPool<PoolableConnector>> poolPair =
                            ConnectorPoolManager.getPool(apiConfiguration, connectorInfo);

                    connectorPoolKey = poolPair.getKey();
                    pool = poolPair.getValue();
                }
                return pool;
            }
        } else {
            return null;
        }
    }

    public Class<? extends Connector> getConnectorClass() {
        return getConnectorInfo().getConnectorClass();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (null != connectorPoolKey) {
            ConnectorPoolManager.dispose(connectorPoolKey);
            connectorPoolKey = null;
        }
    }
}
