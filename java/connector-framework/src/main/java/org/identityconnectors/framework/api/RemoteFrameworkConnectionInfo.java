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
package org.identityconnectors.framework.api;

import java.util.List;
import javax.net.ssl.TrustManager;
import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.security.GuardedString;

/**
 * Encapsulates all the connection information used to connect to a remote
 * instance of the connector framework.
 */
public final class RemoteFrameworkConnectionInfo {
    private final String host;
    private final int port;
    private final GuardedString key;
    private final boolean useSSL;
    private final List<TrustManager> trustManagers;
    private final int timeout;

    /**
     * Creates a new instance of RemoteFrameworkConnectionInfo, using a clear
     * (non-ssl) connection and a 60-second timeout.
     *
     * @param host
     *            The host to connect to
     * @param port
     *            The port to connect to
     * @param key
     *            The remote framework key
     */
    public RemoteFrameworkConnectionInfo(String host, int port, GuardedString key) {
        this(host, port, key, false, null, 60 * 1000);
    }

    /**
     * Creates a new instance of RemoteFrameworkConnectionInfo.
     *
     * @param host
     *            The host to connect to
     * @param port
     *            The port to connect to
     * @param key
     *            The remote framework key
     * @param useSSL
     *            Set to true if we are to connect via SSL.
     * @param trustManagers
     *            List of {@link TrustManager}'s to use for establising the SSL
     *            connection. May be null or empty, in which case the default
     *            installed providers for the JVM will be used. Ignored if
     *            'useSSL' is false.
     * @param timeout
     *            The timeout to use (in milliseconds). A value of 0 means
     *            infinite timeout;
     */
    public RemoteFrameworkConnectionInfo(String host, int port, GuardedString key, boolean useSSL,
            List<TrustManager> trustManagers, int timeout) {
        Assertions.nullCheck(host, "host");
        Assertions.nullCheck(key, "key");
        this.host = host;
        this.port = port;
        this.key = key;
        this.useSSL = useSSL;
        this.trustManagers = CollectionUtil.newReadOnlyList(trustManagers);
        this.timeout = timeout;
    }

    /**
     * Returns the host to connect to.
     *
     * @return The host to connect to.
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the port to connect to.
     *
     * @return The port to connect to.
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the remote framework key.
     *
     * @return the remote framework key.
     */
    public GuardedString getKey() {
        return key;
    }

    /**
     * Returns true if we are to use SSL to connect.
     *
     * @return true if we are to use SSL to connect.
     */
    public boolean getUseSSL() {
        return useSSL;
    }

    /**
     * Returns the list of {@link TrustManager}'s. to use when establishing the
     * connection.
     *
     * @return The list of {@link TrustManager}'s.
     */
    public List<TrustManager> getTrustManagers() {
        return trustManagers;
    }

    /**
     * Returns the timeout (in milliseconds) to use for the connection. A value
     * of zero means infinite timeout.
     *
     * @return the timeout (in milliseconds) to use for the connection.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof RemoteFrameworkConnectionInfo) {
            RemoteFrameworkConnectionInfo other = (RemoteFrameworkConnectionInfo) o;
            if (!getHost().equals(other.getHost())) {
                return false;
            }
            if (getPort() != other.getPort()) {
                return false;
            }
            if (getUseSSL() != other.getUseSSL()) {
                return false;
            }
            if (!getTrustManagers().equals(other.getTrustManagers())) {
                return false;
            }
            if (!getKey().equals(other.getKey())) {
                return false;
            }
            if (getTimeout() != other.getTimeout()) {
                return false;
            }

            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return host.hashCode() ^ port;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "{host=" + host + ", port=" + port + "}";
    }

}
