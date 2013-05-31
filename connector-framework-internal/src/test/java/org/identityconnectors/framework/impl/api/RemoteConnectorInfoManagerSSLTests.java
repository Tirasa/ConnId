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


import java.io.ByteArrayInputStream;
import java.io.File;
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
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

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
            File bundlesDir = getTestBundlesDir();
            File file = new File(bundlesDir,name);
            byte [] bytes = IOUtil.readFileBytes(file);
            KeyStore store = KeyStore.getInstance("PKCS12");
            store.load(new ByteArrayInputStream(bytes), "changeit".toCharArray());
            return store;
        }
        catch (Exception e) {
            throw ConnectorException.wrap(e);
        }
    }

    private class MyTrustManager implements X509TrustManager {
        private String _keyStoreName;

        public MyTrustManager(String name) {
            _keyStoreName = name;
        }

        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if ( o instanceof MyTrustManager ) {
                MyTrustManager other = (MyTrustManager)o;
                return _keyStoreName.equals(other._keyStoreName);
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
        private void checkTrusted(X509Certificate[] chain)
        throws CertificateException {
            KeyStore store = loadKeyStoreResource(_keyStoreName);
            try {
                if ( store.getCertificateAlias(chain[0]) == null ) {
                    throw new CertificateException();
                }
            }
            catch (CertificateException e) {
                throw e;
            }
            catch (Exception e) {
                throw new CertificateException(e);
            }
        }

    }

    private class MyKeyManager implements X509KeyManager {

        private String _keyStoreName;

        public MyKeyManager(String name) {
            _keyStoreName = name;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if ( o instanceof MyKeyManager ) {
                MyKeyManager other = (MyKeyManager)o;
                return _keyStoreName.equals(other._keyStoreName);
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
                KeyStore store = loadKeyStoreResource(_keyStoreName);
                String alias = store.aliases().nextElement();
                return new X509Certificate[]{
                        (X509Certificate)store.getCertificateChain(alias)[0]};
            }
            catch (Exception e) {
                throw ConnectorException.wrap(e);
            }
        }

        @Override
        public String[] getClientAliases(String keyType, Principal[] issuers) {
            return new String[]{"myalias"};
        }

        @Override
        public PrivateKey getPrivateKey(String a) {
            try {
                KeyStore store = loadKeyStoreResource(_keyStoreName);
                String alias = store.aliases().nextElement();
                return (PrivateKey)store.getKey(
                        alias,
                        "changeit".toCharArray());
            } catch (Exception e) {
                throw ConnectorException.wrap(e);
            }
        }

        @Override
        public String[] getServerAliases(String keyType, Principal[] issuers) {
            return new String[]{"mykey"};
        }


    }


    private static ConnectorServer _server;



    /**
     * To be overridden by subclasses to get different ConnectorInfoManagers
     * @return
     * @throws Exception
     */
    @Override
    protected ConnectorInfoManager getConnectorInfoManager() throws Exception {
        List<URL> urls = getTestBundles();

        final int PORT = 8761;

        TrustManager clientTrustManager =
            new MyTrustManager("server.pfx");
        KeyManager serverKeyManager =
            new MyKeyManager("server.pfx");

        synchronized (RemoteConnectorInfoManagerSSLTests.class) {
            if (null == _server) {
                _server = ConnectorServer.newInstance();
                _server.setBundleURLs(urls);
                _server.setPort(PORT);
                _server.setKeyHash(SecurityUtil.computeBase64SHA1Hash("changeit".toCharArray()));
                _server.setUseSSL(true);
                _server.setKeyManagers(CollectionUtil.newList(serverKeyManager));
                _server.setIfAddress(InetAddress.getByName("127.0.0.1"));
                _server.start();
            }
        }
        ConnectorInfoManagerFactory fact = ConnectorInfoManagerFactory.getInstance();

        RemoteFrameworkConnectionInfo connInfo = new
        RemoteFrameworkConnectionInfo("127.0.0.1",PORT,
                new GuardedString("changeit".toCharArray()),
                true,
                CollectionUtil.newList(clientTrustManager),
                60000);

        ConnectorInfoManager manager = fact.getRemoteManager(connInfo);

        return manager;
    }

    @Override
    protected void shutdownConnnectorInfoManager() {
        synchronized (RemoteConnectorInfoManagerSSLTests.class) {
            if (_server != null) {
                _server.stop();
                _server = null;
            }
        }
        // These are initialized by the connector server.
        ConnectorFacadeFactory.getInstance().dispose();
        ConnectorInfoManagerFactory.getInstance().clearLocalCache();
    }

}
