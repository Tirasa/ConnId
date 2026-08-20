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
 * Portions Copyrighted 2024 ConnId
 */
package org.identityconnectors.framework.impl.api;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.IOUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.common.security.SecurityUtil;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.api.RemoteFrameworkConnectionInfo;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.server.ConnectorServer;

public class RemoteConnectorInfoManagerSSLTests extends ConnectorInfoManagerTestBase {

    private KeyStore loadKeyStoreResource(String name) {
        try {
            byte[] bytes = IOUtil.readFileBytes(getTestBundlesDir().toPath().resolve(name));
            KeyStore store = KeyStore.getInstance("PKCS12");
            store.load(new ByteArrayInputStream(bytes), "changeit".toCharArray());
            return store;
        } catch (Exception e) {
            throw ConnectorException.wrap(e);
        }
    }

    private class MyTrustManager extends X509ExtendedTrustManager {

        private final String keyStoreName;

        MyTrustManager(String name) {
            keyStoreName = name;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof MyTrustManager other) {
                return keyStoreName.equals(other.keyStoreName);
            }
            return false;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            checkTrusted(chain);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            checkTrusted(chain);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        private void checkTrusted(X509Certificate[] chain) throws CertificateException {
            KeyStore store = loadKeyStoreResource(keyStoreName);
            try {
                if (store.getCertificateAlias(chain[0]) == null) {
                    throw new CertificateException();
                }
            } catch (CertificateException e) {
                throw e;
            } catch (Exception e) {
                throw new CertificateException(e);
            }
        }

        @Override
        public void checkClientTrusted(
                final X509Certificate[] xcs, final String string, final Socket socket) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(
                final X509Certificate[] xcs, final String string, final Socket socket) throws CertificateException {
        }

        @Override
        public void checkClientTrusted(
                final X509Certificate[] xcs, final String string, final SSLEngine ssle) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(
                final X509Certificate[] xcs, final String string, final SSLEngine ssle) throws CertificateException {
        }
    }

    private class MyKeyManager extends X509ExtendedKeyManager {

        private final String keyStoreName;

        MyKeyManager(String name) {
            keyStoreName = name;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof MyKeyManager) {
                MyKeyManager other = (MyKeyManager) o;
                return keyStoreName.equals(other.keyStoreName);
            }
            return false;
        }

        @Override
        public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
            return "mykey";
        }

        @Override
        public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
            return "mykey";
        }

        @Override
        public X509Certificate[] getCertificateChain(String a) {
            try {
                KeyStore store = loadKeyStoreResource(keyStoreName);
                String alias = store.aliases().nextElement();
                return new X509Certificate[] { (X509Certificate) store.getCertificateChain(alias)[0] };
            } catch (Exception e) {
                throw ConnectorException.wrap(e);
            }
        }

        @Override
        public String[] getClientAliases(String keyType, Principal[] issuers) {
            return new String[] { "myalias" };
        }

        @Override
        public PrivateKey getPrivateKey(String a) {
            try {
                KeyStore store = loadKeyStoreResource(keyStoreName);
                String alias = store.aliases().nextElement();
                return (PrivateKey) store.getKey(alias, "changeit".toCharArray());
            } catch (Exception e) {
                throw ConnectorException.wrap(e);
            }
        }

        @Override
        public String[] getServerAliases(String keyType, Principal[] issuers) {
            return new String[] { "mykey" };
        }

    }

    private static ConnectorServer SERVER;

    /**
     * To be overridden by subclasses to get different ConnectorInfoManagers
     *
     * @return
     * @throws Exception
     */
    @Override
    protected ConnectorInfoManager getConnectorInfoManager() throws Exception {
        List<URL> urls = getTestBundles();

        final int port = 8761;

        TrustManager clientTrustManager = new MyTrustManager("server.pfx");
        KeyManager serverKeyManager = new MyKeyManager("server.pfx");

        synchronized (RemoteConnectorInfoManagerSSLTests.class) {
            if (null == SERVER) {
                SERVER = ConnectorServer.newInstance();
                SERVER.setBundleURLs(urls);
                SERVER.setPort(port);
                SERVER.setKeyHash(SecurityUtil.computeBase64SHA1Hash("changeit".toCharArray()));
                SERVER.setUseSSL(true);
                SERVER.setKeyManagers(CollectionUtil.newList(serverKeyManager));
                SERVER.setIfAddress(InetAddress.getByName("127.0.0.1"));
                SERVER.start();
            }
        }
        ConnectorInfoManagerFactory fact = ConnectorInfoManagerFactory.getInstance();

        RemoteFrameworkConnectionInfo connInfo = new RemoteFrameworkConnectionInfo("127.0.0.1", port,
                new GuardedString("changeit".toCharArray()),
                true,
                CollectionUtil.newList(clientTrustManager),
                0);

        ConnectorInfoManager manager = fact.getRemoteManager(connInfo);

        return manager;
    }

    @Override
    protected void shutdownConnnectorInfoManager() {
        synchronized (RemoteConnectorInfoManagerSSLTests.class) {
            if (SERVER != null) {
                SERVER.stop();
                SERVER = null;
            }
        }
        // These are initialized by the connector server.
        ConnectorFacadeFactory.getInstance().dispose();
        ConnectorInfoManagerFactory.getInstance().clearLocalCache();
    }
}
