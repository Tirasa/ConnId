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
package org.identityconnectors.framework.impl.api;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.impl.api.local.ConnectorPoolManager;
import org.identityconnectors.framework.impl.api.local.LocalConnectorFacadeImpl;
import org.identityconnectors.framework.impl.api.local.LocalConnectorInfoImpl;
import org.identityconnectors.framework.impl.api.remote.RemoteConnectorFacadeImpl;
import org.identityconnectors.framework.impl.api.remote.RemoteConnectorInfoImpl;

public class ConnectorFacadeFactoryImpl extends ConnectorFacadeFactory {

    private static final Log LOG = Log.getLog(ConnectorFacadeFactoryImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectorFacade newInstance(final APIConfiguration config) {
        ConnectorFacade ret = null;
        final APIConfigurationImpl impl = (APIConfigurationImpl) config;
        final AbstractConnectorInfo connectorInfo = impl.getConnectorInfo();
        if (connectorInfo instanceof LocalConnectorInfoImpl) {
            final LocalConnectorInfoImpl localInfo = (LocalConnectorInfoImpl) connectorInfo;
            try {
                // create a new Provisioner.
                ret = new LocalConnectorFacadeImpl(localInfo, impl);

            } catch (Exception ex) {
                String connector = impl.getConnectorInfo().getConnectorKey().toString();
                LOG.error(ex, "Failed to create new connector facade: {0}, {1}", connector, config);
                throw ConnectorException.wrap(ex);
            }
        } else if (connectorInfo instanceof RemoteConnectorInfoImpl) {
            ret = new RemoteConnectorFacadeImpl(impl);
        } else {
            throw new IllegalArgumentException("Unknown ConnectorInfo type");
        }
        return ret;
    }

    @Override
    public ConnectorFacade newInstance(final ConnectorInfo connectorInfo, String config) {
        ConnectorFacade ret = null;
        if (connectorInfo instanceof LocalConnectorInfoImpl) {
            try {
                // create a new Provisioner.
                ret = new LocalConnectorFacadeImpl((LocalConnectorInfoImpl) connectorInfo, config);

            } catch (Exception ex) {
                String connector = connectorInfo.getConnectorKey().toString();
                LOG.error(ex, "Failed to create new connector facade: {0}, {1}", connector, config);
                throw ConnectorException.wrap(ex);
            }
        } else if (connectorInfo instanceof RemoteConnectorInfoImpl) {
            ret = new RemoteConnectorFacadeImpl((RemoteConnectorInfoImpl) connectorInfo, config);
        } else {
            throw new IllegalArgumentException("Unknown ConnectorInfo type");
        }
        return ret;
    }

    /**
     * Shut down of all object pools and other resources associated with this
     * class.
     */
    @Override
    public void dispose() {
    	// Disposal of connector factory means shutdown of all connector pools.
    	// This is the end. No more connector instances will be created.
        ConnectorPoolManager.shutdown();
    }

}
