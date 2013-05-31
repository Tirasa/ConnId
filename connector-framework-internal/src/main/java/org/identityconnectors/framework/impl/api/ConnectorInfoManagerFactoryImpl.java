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

    private final Map<LocalManagerKey, ConnectorInfoManager> localManagerCache =
            new HashMap<LocalManagerKey, ConnectorInfoManager>();

    private final Map<RemoteManagerKey, RemoteConnectorInfoManagerImpl> remoteManagerCache =
            new HashMap<RemoteManagerKey, RemoteConnectorInfoManagerImpl>();

    public ConnectorInfoManagerFactoryImpl() {
    }

    @Override
    public void clearLocalCache() {
        synchronized (localManagerCache) {
            localManagerCache.clear();
        }
    }

    @Override
    public void clearRemoteCache() {
        synchronized (remoteManagerCache) {
            remoteManagerCache.clear();
        }
    }

    @Override
    public ConnectorInfoManager getLocalManager(URL... urls) throws ConfigurationException {
        return getLocalManager(Arrays.asList(urls), null);
    }

    public ConnectorInfoManager getLocalManager(List<URL> urls, ClassLoader bundleParentClassLoader)
            throws ConfigurationException {
        Assertions.nullCheck(urls, "urls");
        for (URL url : urls) {
            Assertions.nullCheck(url, "urls");
        }
        if (bundleParentClassLoader == null) {
            bundleParentClassLoader = ConnectorInfoManagerFactory.class.getClassLoader();
        }
        LocalManagerKey key = new LocalManagerKey(urls, bundleParentClassLoader);
        synchronized (localManagerCache) {
            ConnectorInfoManager rv = localManagerCache.get(key);
            if (rv == null) {
                rv = new LocalConnectorInfoManagerImpl(urls, bundleParentClassLoader);
            }
            localManagerCache.put(key, rv);
            return rv;
        }
    }

    @Override
    public ConnectorInfoManager getRemoteManager(RemoteFrameworkConnectionInfo info)
            throws ConfigurationException {
        RemoteManagerKey key = new RemoteManagerKey(info);
        synchronized (remoteManagerCache) {
            RemoteConnectorInfoManagerImpl rv = remoteManagerCache.get(key);
            if (rv == null) {
                rv = new RemoteConnectorInfoManagerImpl(info);
            }
            remoteManagerCache.put(key, rv);
            return rv.derive(info);
        }
    }

    public ConnectorInfoManager getUnCheckedRemoteManager(RemoteFrameworkConnectionInfo info) {
        RemoteManagerKey key = new RemoteManagerKey(info);
        synchronized (remoteManagerCache) {
            RemoteConnectorInfoManagerImpl rv = remoteManagerCache.get(key);
            if (rv == null) {
                rv = new RemoteConnectorInfoManagerImpl(info, false);
            }
            remoteManagerCache.put(key, rv);
            return rv;
        }
    }

    private static final class LocalManagerKey {

        private final List<URL> urls;
        private final ClassLoader bundleParentClassLoader;

        public LocalManagerKey(List<URL> urls, ClassLoader bundleParentClassLoader) {
            this.urls = CollectionUtil.newReadOnlyList(urls);
            this.bundleParentClassLoader = bundleParentClassLoader;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LocalManagerKey) {
                LocalManagerKey other = (LocalManagerKey) obj;
                if (!urls.equals(other.urls)) {
                    return false;
                }
                if (!bundleParentClassLoader.equals(other.bundleParentClassLoader)) {
                    return false;
                }
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return urls.hashCode() ^ bundleParentClassLoader.hashCode();
        }
    }

    private static final class RemoteManagerKey {

        private final String host;
        private final int port;

        public RemoteManagerKey(RemoteFrameworkConnectionInfo info) {
            host = info.getHost();
            port = info.getPort();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof RemoteManagerKey) {
                RemoteManagerKey other = (RemoteManagerKey) o;
                if (!host.equals(other.host)) {
                    return false;
                }
                if (port != other.port) {
                    return false;
                }
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return host.hashCode() ^ port;
        }
    }
}
