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
package org.identityconnectors.framework.api;

import org.identityconnectors.framework.common.exceptions.ConnectorException;

/**
 * Allows an application to obtain a {@link ConnectorFacade connector instance}.
 * Manages a pool of connector instances.
 *
 * @author Will Droste
 * @since 1.0
 */
public abstract class ConnectorFacadeFactory {

    // At some point we might make this pluggable, but for now, hard-code
    private static final String IMPL_NAME =
            "org.identityconnectors.framework.impl.api.ConnectorFacadeFactoryImpl";

    private static final String IMPL_NAME_MANAGED =
            "org.identityconnectors.framework.impl.api.ManagedConnectorFacadeFactoryImpl";

    private static ConnectorFacadeFactory instance;
    private static ConnectorFacadeFactory managedInstance;

    /**
     * Get the singleton instance of the {@link ConnectorFacadeFactory}.
     */
    public static synchronized ConnectorFacadeFactory getInstance() {
        if (instance == null) {
            try {
                final Class<?> clazz = Class.forName(IMPL_NAME);
                final Object object = clazz.newInstance();
                instance = ConnectorFacadeFactory.class.cast(object);
            } catch (Exception e) {
                throw ConnectorException.wrap(e);
            }
        }
        return instance;
    }

    /**
     * Get the singleton instance of the stateful {@link ConnectorFacadeFactory}.
     *
     * @since 1.4
     */
    public static synchronized ConnectorFacadeFactory getManagedInstance() {
        if (managedInstance == null) {
            try {
                final Class<?> clazz = Class.forName(IMPL_NAME_MANAGED);
                final Object object = clazz.newInstance();
                managedInstance = ConnectorFacadeFactory.class.cast(object);
            } catch (Exception e) {
                throw ConnectorException.wrap(e);
            }
        }
        return managedInstance;
    }

    /**
     * Shut down of all connector pools, resources, etc.
     * The framework will release all the resources.
     * No operations will be possible until the framework is re-initialized.
     */
    public abstract void dispose();

    /**
     * Get a new instance of {@link ConnectorFacade}.
     *
     * @param config
     *            all the configuration that the framework, connector, and
     *            pooling needs.
     * @return {@link ConnectorFacade} to call API operations against.
     */
    public abstract ConnectorFacade newInstance(APIConfiguration config);

    /**
     * Get a new instance of {@link ConnectorFacade}.
     *
     * @param connectorInfo
     *            TODO Add JavaDoc later
     * @param config
     *            all the configuration that the framework, connector, and
     *            pooling needs. It's a Base64 serialised APIConfiguration
     *            instance.
     * @return {@link ConnectorFacade} to call API operations against.
     * @since 1.4
     */
    public abstract ConnectorFacade newInstance(ConnectorInfo connectorInfo, String config);
}
