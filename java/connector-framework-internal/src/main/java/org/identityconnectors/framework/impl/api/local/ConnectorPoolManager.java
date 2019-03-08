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
 * Portions Copyrighted 2018 ConnId
 * Portions Copyrighted 2019 Evolveum
 */
package org.identityconnectors.framework.impl.api.local;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.identityconnectors.common.Pair;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.pooling.ObjectPoolConfiguration;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.serializer.SerializerUtil;
import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.ConfigurationPropertiesImpl;
import org.identityconnectors.framework.impl.api.local.operations.OperationalContext;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.PoolableConnector;

public class ConnectorPoolManager {

    public static class ConnectorPoolKey {

        private final ConnectorKey connectorKey;

        private final ConfigurationPropertiesImpl configProperties;

        private final ObjectPoolConfiguration poolingConfig;

        public ConnectorPoolKey(final ConnectorKey connectorKey,
                final ConfigurationPropertiesImpl configProperties,
                final ObjectPoolConfiguration poolingConfig) {

            this.connectorKey = connectorKey;
            this.configProperties = configProperties;
            this.poolingConfig = poolingConfig;
        }

        @Override
        public int hashCode() {
            return connectorKey.hashCode();
        }

        @Override
        public boolean equals(final Object object) {
            if (object instanceof ConnectorPoolKey) {
                final ConnectorPoolKey other = (ConnectorPoolKey) object;
                if (!connectorKey.equals(other.connectorKey)) {
                    return false;
                }
                if (!configProperties.equals(other.configProperties)) {
                    return false;
                }
                if (!poolingConfig.equals(other.poolingConfig)) {
                    return false;
                }
                return true;
            }
            return false;
        }
    }

    private static class ConnectorPoolHandler implements ObjectPoolHandler<PoolableConnector> {

        private final APIConfigurationImpl apiConfiguration;

        private final LocalConnectorInfoImpl localConnectorInfo;

        private final OperationalContext context;

        public ConnectorPoolHandler(
                final APIConfigurationImpl apiConfiguration,
                final LocalConnectorInfoImpl localInfo) {

            this.apiConfiguration = apiConfiguration;
            this.localConnectorInfo = localInfo;
            if (localConnectorInfo.isConfigurationStateless()) {
                this.context = null;
            } else {
                this.context = new OperationalContext(localInfo, apiConfiguration);
            }
        }

        @Override
        public ObjectPoolConfiguration validate(ObjectPoolConfiguration original) {
            ObjectPoolConfiguration configuration =
                    (ObjectPoolConfiguration) SerializerUtil.cloneObject(original);
            configuration.validate();
            return configuration;
        }

        @Override
        public PoolableConnector makeObject() {
            // setup classloader for constructor and initialization of config bean and connector
            ThreadClassLoaderManager.getInstance().pushClassLoader(
                    localConnectorInfo.getConnectorClass().getClassLoader());
            try {

                Class<? extends Connector> clazz = localConnectorInfo.getConnectorClass();
                PoolableConnector connector = null;
                if (PoolableConnector.class.isAssignableFrom(clazz)) {

                    Configuration config = null;
                    if (null == context) {
                        config =
                                JavaClassProperties.createBean(apiConfiguration
                                        .getConfigurationProperties(), localConnectorInfo
                                                .getConnectorConfigurationClass());
                    } else {
                        config = context.getConfiguration();
                    }

                    connector = (PoolableConnector) clazz.newInstance();
                    connector.init(config);
                } else {
                    throw new ConnectorException("The Connector is not PoolableConnector: "
                            + localConnectorInfo.getConnectorKey());
                }
                return connector;
            } catch (Exception e) {
                throw ConnectorException.wrap(e);
            } finally {
                ThreadClassLoaderManager.getInstance().popClassLoader();
            }
        }

        @Override
        public void testObject(PoolableConnector object) {
            ThreadClassLoaderManager.getInstance().pushClassLoader(
                    localConnectorInfo.getConnectorClass().getClassLoader());
            try {
                object.checkAlive();
            } finally {
                ThreadClassLoaderManager.getInstance().popClassLoader();
            }
        }

        @Override
        public void disposeObject(PoolableConnector object) {
            ThreadClassLoaderManager.getInstance().pushClassLoader(
                    localConnectorInfo.getConnectorClass().getClassLoader());
            try {
                object.dispose();
            } finally {
                ThreadClassLoaderManager.getInstance().popClassLoader();
            }
        }

        @Override
        public void shutdown() {
            if (null != context) {
                context.dispose();
            }
        }
    }

    /**
     * Cache of the various POOLS..
     */
    private static final ConcurrentMap<ConnectorPoolKey, ObjectPool<PoolableConnector>> POOLS =
            new ConcurrentHashMap<ConnectorPoolKey, ObjectPool<PoolableConnector>>();

    private static final Log LOG = Log.getLog(ConnectorPoolManager.class);

    /**
     * Get a object pool for this connector if it supports connector pooling.
     */
    public static Pair<ConnectorPoolKey, ObjectPool<PoolableConnector>> getPool(
            final APIConfigurationImpl impl, final LocalConnectorInfoImpl localInfo) {
        try {
            return getPool2(impl, localInfo);
        } catch (Exception e) {
            throw ConnectorException.wrap(e);
        }
    }

    /**
     * Get a object pool for this connector if it was created before.
     */
    public static ObjectPool<PoolableConnector> getPool(final ConnectorPoolKey connectorPoolKey) {
        return POOLS.get(connectorPoolKey);
    }

    private static Pair<ConnectorPoolKey, ObjectPool<PoolableConnector>> getPool2(
            final APIConfigurationImpl impl, final LocalConnectorInfoImpl localInfo)
            throws InstantiationException, IllegalAccessException {
        // determine if this connector wants generic connector pooling..
        if (impl.isConnectorPoolingSupported()) {
            ConnectorPoolKey key =
                    new ConnectorPoolKey(impl.getConnectorInfo().getConnectorKey(), impl
                            .getConfigurationProperties(), impl.getConnectorPoolConfiguration());

            synchronized (POOLS) {
                // get the pool associated..
                ObjectPool<PoolableConnector> pool = POOLS.get(key);
                // create a new pool if it doesn't exist..
                if (pool == null) {
                    String poolName = impl.getConnectorInfo().getConnectorKey().toString();
                    LOG.info("Creating new pool: {0}", poolName);
                    // this instance is strictly used for the pool..
                    pool = new ObjectPool<PoolableConnector>(
                            new ConnectorPoolHandler(impl, localInfo), impl.getConnectorPoolConfiguration());
                    pool.setPoolName(poolName);
                    // add back to the map of POOLS..

                    ObjectPool<PoolableConnector> previousPool = POOLS.putIfAbsent(key, pool);
                    // Use the pool made by other thread
                    if (previousPool != null) {
                        pool = previousPool;
                    }
                }
                return Pair.of(key, pool);
            }
        } else if (!localInfo.isConfigurationStateless()) {
            return Pair.of(new ConnectorPoolKey(impl.getConnectorInfo().getConnectorKey(), impl
                    .getConfigurationProperties(), impl.getConnectorPoolConfiguration()), null);
        }
        return Pair.of(null, null);
    }

    /**
     * Disposes of all connector instances of a particular connector.
     * This cleans up the pool and marks all existing objects for disposal.
     * Unlike shutdown, creating new connector instances will be possible after
     * pool content disposal. This is needed, as this pool instance may still
     * be "in circulation" and new new objects may be created due to race
     * conditions. We do not really mind if a connector is created while
     * the pool is disposing. But we really want to make sure that all connector
     * instances will be disposed of immediately after they are returned to the pool.
     */
    public static void dispose(final ConnectorPoolKey connectorPoolKey) {
        synchronized (POOLS) {
            ObjectPool<PoolableConnector> pool = POOLS.remove(connectorPoolKey);
            if (null != pool) {
                try {
                    pool.disposeAllObjects();
                } catch (Exception e) {
                    LOG.warn(e, "Failed to dispose objects in pool {0}: {1}", pool.getPoolName(), e.getMessage());
                }
            }
        }
    }

    /**
     * Shuts down the connector pool. No more connectors will be created.
     */
    public static void shutdown() {
        LOG.info("Shutting down all connector pools");
        synchronized (POOLS) {
            // close each pool..
            for (ObjectPool<PoolableConnector> pool : POOLS.values()) {
                try {
                    pool.shutdown();
                } catch (Exception e) {
                    LOG.warn(e, "Failed to shutdown pool {0}: {1}", pool.getPoolName(), e.getMessage());
                }
            }
            // clear the map of all POOLS..
            POOLS.clear();
        }
    }
}
