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
package org.identityconnectors.framework.impl.api;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.api.RemoteFrameworkConnectionInfo;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.impl.api.local.LocalConnectorInfoManagerImpl;
import org.identityconnectors.framework.impl.api.remote.RemoteConnectorInfoManagerImpl;

public class ConnectorInfoManagerFactoryImpl extends ConnectorInfoManagerFactory {

    private final Map<LocalManagerKey,ConnectorInfoManager>
            _localManagerCache = new HashMap<LocalManagerKey,ConnectorInfoManager>();

    private final Map<RemoteManagerKey,RemoteConnectorInfoManagerImpl>
            _remoteManagerCache = new HashMap<RemoteManagerKey,RemoteConnectorInfoManagerImpl>();

    public ConnectorInfoManagerFactoryImpl() {
    }
    
    @Override
    public void clearLocalCache() {
        synchronized (_localManagerCache) {
            _localManagerCache.clear();
        }
    }
    @Override
    public void clearRemoteCache() {
        synchronized (_remoteManagerCache) {
            _remoteManagerCache.clear();
        }
    }
    
    @Override
    public ConnectorInfoManager getLocalManager(URL... urls) throws ConfigurationException {
        return getLocalManager(Arrays.asList(urls), null);
    }
    
    public ConnectorInfoManager getLocalManager(List<URL> urls, ClassLoader bundleParentClassLoader) throws ConfigurationException {
        Assertions.nullCheck(urls, "urls");
        for (URL url : urls) {
            Assertions.nullCheck(url, "urls");            
        }
        if (bundleParentClassLoader == null) {
            bundleParentClassLoader = ConnectorInfoManagerFactory.class.getClassLoader();
        }
        LocalManagerKey key = new LocalManagerKey(urls, bundleParentClassLoader);
        synchronized (_localManagerCache) {
            ConnectorInfoManager rv = _localManagerCache.get(key);
            if ( rv == null ) {
                rv = new LocalConnectorInfoManagerImpl(urls, bundleParentClassLoader);
            }
            _localManagerCache.put(key, rv);
            return rv;    
        }
    }
    
    @Override
    public ConnectorInfoManager getRemoteManager(RemoteFrameworkConnectionInfo info) throws ConfigurationException {
        RemoteManagerKey key = new RemoteManagerKey(info);
        synchronized (_remoteManagerCache) {
            RemoteConnectorInfoManagerImpl rv = _remoteManagerCache.get(key);
            if ( rv == null ) {
                rv = new RemoteConnectorInfoManagerImpl(info);
            }
            _remoteManagerCache.put(key, rv);
            return rv.derive(info);
        }
    }

    private static final class LocalManagerKey {
        
        private final List<URL> _urls;
        private final ClassLoader _bundleParentClassLoader;
        
        public LocalManagerKey(List<URL> urls, ClassLoader bundleParentClassLoader) {
            _urls = CollectionUtil.newReadOnlyList(urls);
            _bundleParentClassLoader = bundleParentClassLoader;
        }

        @Override
        public boolean equals(Object obj) {
            if ( obj instanceof LocalManagerKey ) {
                LocalManagerKey other = (LocalManagerKey)obj;
                if (!_urls.equals(other._urls)) {
                    return false;
                }
                if (!_bundleParentClassLoader.equals(other._bundleParentClassLoader)) {
                    return false;
                }
                return true;
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return _urls.hashCode() ^ _bundleParentClassLoader.hashCode();
        }
    }
    
    private static final class RemoteManagerKey {
        
        private final String _host;
        private final int _port;
        
        public RemoteManagerKey(RemoteFrameworkConnectionInfo info) {
            _host = info.getHost();
            _port = info.getPort();
        }
        
        @Override
        public boolean equals(Object o) {
            if ( o instanceof RemoteManagerKey ) {
                RemoteManagerKey other = (RemoteManagerKey)o;
                if (!_host.equals(other._host)) {
                    return false;
                }
                if (_port != other._port) {
                    return false;
                }
                return true;
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return _host.hashCode()^_port;
        }
    }
}
