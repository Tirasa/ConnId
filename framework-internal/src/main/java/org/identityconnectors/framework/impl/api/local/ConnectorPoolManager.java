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

import java.util.HashMap;
import java.util.Map;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.pooling.ObjectPoolConfiguration;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.ConfigurationPropertiesImpl;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.PoolableConnector;


public class ConnectorPoolManager {

    private static class ConnectorPoolKey {
        private final ConnectorKey _connectorKey;
        private final ConfigurationPropertiesImpl _configProperties;
        private final ObjectPoolConfiguration _poolingConfig;
        public ConnectorPoolKey(ConnectorKey connectorKey,
                ConfigurationPropertiesImpl configProperties,
                ObjectPoolConfiguration poolingConfig) {
            _connectorKey = connectorKey;
            _configProperties = configProperties;
            _poolingConfig = poolingConfig;
        }
        @Override
        public int hashCode() {
            return _connectorKey.hashCode();
        }
        @Override
        public boolean equals(Object o) {
            if ( o instanceof ConnectorPoolKey ) {
                ConnectorPoolKey other = (ConnectorPoolKey)o;
                if (!_connectorKey.equals(other._connectorKey)) {
                    return false;
                }
                if (!_configProperties.equals(other._configProperties)) {
                    return false;
                }
                if (!_poolingConfig.equals(other._poolingConfig)) {
                    return false;
                }
                return true;
            }
            return false;
        }
    }
    
    private static class ConnectorPoolHandler implements ObjectPoolHandler<PoolableConnector> {
        private final APIConfigurationImpl _apiConfiguration;
        private final LocalConnectorInfoImpl _localInfo;
        public ConnectorPoolHandler(APIConfigurationImpl apiConfiguration,
                LocalConnectorInfoImpl localInfo) {
            _apiConfiguration = apiConfiguration;
            _localInfo        = localInfo;
        }
        public PoolableConnector newObject() {
            //setup classloader for constructor and
            //initialization of config bean and connector
            ThreadClassLoaderManager.getInstance().pushClassLoader(_localInfo.getConnectorClass().getClassLoader());
            try {
                Configuration config =
                    JavaClassProperties.createBean(_apiConfiguration.getConfigurationProperties(), 
                        _localInfo.getConnectorConfigurationClass());
                PoolableConnector connector =
                    (PoolableConnector)(_localInfo.getConnectorClass().newInstance());
                connector.init(config);
                return connector;
            }
            catch (Exception e) {
                throw ConnectorException.wrap(e);
            }
            finally {
                ThreadClassLoaderManager.getInstance().popClassLoader();
            }
        }
        public void testObject(PoolableConnector object) {
            ThreadClassLoaderManager.getInstance().pushClassLoader(_localInfo.getConnectorClass().getClassLoader());
            try {
                object.checkAlive();
            }
            finally {
                ThreadClassLoaderManager.getInstance().popClassLoader();
            }
        }
        public void disposeObject(PoolableConnector object) {
            ThreadClassLoaderManager.getInstance().pushClassLoader(_localInfo.getConnectorClass().getClassLoader());
            try {
                object.dispose();
            }
            finally {
                ThreadClassLoaderManager.getInstance().popClassLoader();
            }
        }
    }
    
    /**
     * Cache of the various _pools..
     */
    private static final Map<ConnectorPoolKey, ObjectPool<PoolableConnector>>
        _pools = new HashMap<ConnectorPoolKey, ObjectPool<PoolableConnector>>();

    private static Log _log = Log.getLog(ConnectorPoolManager.class);
    
    /**
     * Get a object pool for this connector if it supports connector pooling.
     */
    public static ObjectPool<PoolableConnector> getPool(APIConfigurationImpl impl, 
            LocalConnectorInfoImpl localInfo) {
        try {
            return getPool2(impl,localInfo);
        }
        catch (Exception e) {
            throw ConnectorException.wrap(e);
        }
    }
    private static ObjectPool<PoolableConnector> getPool2(APIConfigurationImpl impl, LocalConnectorInfoImpl localInfo)
    throws InstantiationException, IllegalAccessException {
        ObjectPool<PoolableConnector> pool = null;
        // determine if this connector wants generic connector pooling..
        if (impl.isConnectorPoolingSupported()) {
            ConnectorPoolKey key =
                new ConnectorPoolKey(
                        impl.getConnectorInfo().getConnectorKey(),
                        impl.getConfigurationProperties(),
                        impl.getConnectorPoolConfiguration());
            
            synchronized (_pools) {   
                // get the pool associated..
                pool = _pools.get(key);
                // create a new pool if it doesn't exist..
                if (pool == null) {
                    _log.info("Creating new pool: {0}",
                            impl.getConnectorInfo().getConnectorKey());
                    // this instance is strictly used for the pool..
                    pool = new ObjectPool<PoolableConnector>(
                            new ConnectorPoolHandler(impl,localInfo),
                            impl.getConnectorPoolConfiguration());
                    // add back to the map of _pools..
                    _pools.put(key, pool);
                }
            }
        }
        return pool;
    }
    
    public static void dispose() {
        synchronized (_pools) {
            // close each pool..
            for (ObjectPool<PoolableConnector> pool : _pools.values()) {
                try {
                    pool.shutdown();
                } catch (Exception e) {
                    _log.warn(e, "Failed to close pool: {0}", pool);
                }
            }
            // clear the map of all _pools..
            _pools.clear();
        }
    }

}
