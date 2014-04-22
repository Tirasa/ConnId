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
 * Portions Copyrighted 2010-2014 ForgeRock AS.
 */
package org.identityconnectors.framework.impl.api.local.operations;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.ResultsHandlerConfiguration;
import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.local.JavaClassProperties;
import org.identityconnectors.framework.impl.api.local.LocalConnectorInfoImpl;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.StatefulConfiguration;

/**
 * OperationalContext - base class for operations that do not require a
 * connector instance.
 */
public class OperationalContext {

    private static final Log LOG = Log.getLog(OperationalContext.class);

    /**
     * ConnectorInfo
     */
    protected final LocalConnectorInfoImpl connectorInfo;

    /**
     * Contains the {@link Connector} {@link Configuration}.
     */
    protected final APIConfigurationImpl apiConfiguration;

    private volatile Configuration configuration;

    /**
     * Creates a new OperationalContext but it does not initiates the
     * Configuration because the {@link #getConnectorInfo()} method must do it
     * when it's called from a block where the classloader of the Thread is set
     * to Connector.
     *
     * @param connectorInfo
     * @param apiConfiguration
     */
    public OperationalContext(final LocalConnectorInfoImpl connectorInfo,
            final APIConfigurationImpl apiConfiguration) {
        this.connectorInfo = connectorInfo;
        this.apiConfiguration = apiConfiguration;

    }

    /*
     * This method must be called when the Bundle ClassLoader is the Thread
     * Context ClassLoader.
     */
    public Configuration getConfiguration() {
        if (null == configuration) {
            synchronized (this) {
                if (null == configuration) {
                    this.configuration =
                            JavaClassProperties.createBean(apiConfiguration
                                    .getConfigurationProperties(), connectorInfo
                                    .getConnectorConfigurationClass());
                }
            }
        }
        return configuration;
    }

    protected LocalConnectorInfoImpl getConnectorInfo() {
        return connectorInfo;
    }

    public ResultsHandlerConfiguration getResultsHandlerConfiguration() {
        return new ResultsHandlerConfiguration(apiConfiguration.getResultsHandlerConfiguration());
    }

    public void dispose() {
        if (configuration instanceof StatefulConfiguration) {
            // dispose it not supposed to throw, but just in case,
            // catch the exception and log it so we know about it
            // but don't let the exception prevent additional
            // cleanup that needs to happen
            try {
                StatefulConfiguration config = (StatefulConfiguration) configuration;
                configuration = null;
                config.release();
            } catch (Exception e) {
                // log this though
                LOG.warn(e, null);
            }
        }
    }
}
