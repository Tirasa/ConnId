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

import java.net.URL;

import org.identityconnectors.framework.common.exceptions.ConnectorException;

/**
 * An application's primary entry point into connectors. Allows an application
 * to load the connector classes from a set of bundles.
 */
public abstract class ConnectorInfoManagerFactory {

    // At some point we might make this pluggable, but for now, hard-code
    private static final String IMPL_NAME =
            "org.identityconnectors.framework.impl.api.ConnectorInfoManagerFactoryImpl";

    private static ConnectorInfoManagerFactory instance;

    /**
     * Returns the instance of this factory.
     *
     * @return The instance of this factory
     */
    public static synchronized ConnectorInfoManagerFactory getInstance() {
        if (instance == null) {
            try {
                Class<?> clazz = Class.forName(IMPL_NAME);
                Object object = clazz.newInstance();
                instance = ConnectorInfoManagerFactory.class.cast(object);
            } catch (Exception e) {
                throw ConnectorException.wrap(e);
            }
        }
        return instance;
    }

    /**
     * Creates the <code>ConnectorInfoManager</code> from a list of bundle URLs.
     * <p>
     * <b>NOTE:</b> The results from this call are automatically cached and
     * keyed by the list of URLs passed in. To clear the cache, call
     * {@link #clearLocalCache}.
     *
     * @param urls
     *            The list of bundle URLs. This list may consist of directories
     *            consisting of un-jarred bundles and/or bundle jars.
     *
     * @return The manager
     * @throws org.identityconnectors.framework.common.exceptions.ConfigurationException
     *             If there was any problem with any of the bundles.
     */
    public abstract ConnectorInfoManager getLocalManager(URL... urls);

    /**
     * Creates the <code>ConnectorInfoManager</code> for a remote framework.
     * <p>
     * <b>NOTE:</b> The results from this call are automatically cached and
     * keyed by the RemoteFrameworkConnectionInfo passed in. To clear the cache,
     * call {@link #clearRemoteCache}.
     *
     * @param info
     *            The connection information.
     *
     * @return The manager
     * @throws RuntimeException
     *             If there was any problem connecting
     */
    public abstract ConnectorInfoManager getRemoteManager(RemoteFrameworkConnectionInfo info);

    /**
     * Creates the <code>ConnectorInfoManager</code> for a remote framework but
     * the connection is not checked and not initialised.
     * <p>
     * NOTICE: This method is an early specification of the Events API for
     * 1.2.x.x version. Use carefully, this package may change before the final
     * 1.2.0.0 release.
     * <p/>
     * As now the {@code ConnectorInfoManager} MUST implement the
     * {@link Runnable} to connect to the remote connector server. <b>NOTE:</b>
     * The results from this call are automatically cached and keyed by the
     * RemoteFrameworkConnectionInfo passed in. To clear the cache, call
     * {@link #clearRemoteCache}.
     *
     * @param info
     *            The connection information.
     *
     * @return The manager
     *
     * @since 1.2
     */
    public abstract ConnectorInfoManager getUnCheckedRemoteManager(
            RemoteFrameworkConnectionInfo info);

    /**
     * Clears the local bundle manager cache. NOTE: Avoid using this method
     * outside of unit testing.
     */
    public abstract void clearLocalCache();

    /**
     * Clears the remote cache. There should be an admin page function which
     * exposes this method for the case where you drop in a new connector in the
     * connector server and reset it.
     */
    public abstract void clearRemoteCache();

}
