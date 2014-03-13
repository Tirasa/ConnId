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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.pooling.ObjectPoolConfiguration;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ResultsHandlerConfiguration;
import org.identityconnectors.framework.api.operations.APIOperation;


public class APIConfigurationImpl implements APIConfiguration {

    // =======================================================================
    // Fields
    // =======================================================================
    /**
     * All configuration related to connector pooling.
     */
    private ObjectPoolConfiguration connectorPoolConfiguration;

    private ResultsHandlerConfiguration resultsHandlerConfiguration;

    private boolean isConnectorPoolingSupported;

    private ConfigurationPropertiesImpl configurationProperties;

    /**
     * Default size of the buffer.
     */
    private int bufferSize = 100;

    /**
     * Map of timeout per operation.
     */
    private Map<Class<? extends APIOperation>, Integer> timeoutMap
    = new HashMap<Class<? extends APIOperation>, Integer>();

    /**
     * Set of supported operations;
     */
    private Set<Class<? extends APIOperation>> supportedOperations;


    /**
     * The connector info from which this was created. Not serialized in this
     * object. Set when returned from the parent
     */
    private transient AbstractConnectorInfo connectorInfo;

    // =======================================================================
    // Constructors
    // =======================================================================

    public APIConfigurationImpl() {
    }

    public APIConfigurationImpl(APIConfigurationImpl other) {
        if (null != other.connectorPoolConfiguration) {
            this.setConnectorPoolConfiguration(new ObjectPoolConfiguration(other.connectorPoolConfiguration));
        }
        if (null != other.resultsHandlerConfiguration) {
            this.setResultsHandlerConfiguration(new ResultsHandlerConfiguration(other.resultsHandlerConfiguration));
        }
        this.isConnectorPoolingSupported = other.isConnectorPoolingSupported;
        ConfigurationPropertiesImpl prop = new ConfigurationPropertiesImpl();
        prop.setProperties(other.getConfigurationProperties().getProperties());
        setConfigurationProperties(prop);

        this.bufferSize = other.bufferSize;
        this.timeoutMap = new HashMap<Class<? extends APIOperation>, Integer>( other.timeoutMap);
        this.supportedOperations = new HashSet<Class<? extends APIOperation>>( other.supportedOperations);

        this.connectorInfo = other.connectorInfo;
    }

    // =======================================================================
    // Internal Methods
    // =======================================================================


    public AbstractConnectorInfo getConnectorInfo() {
        return connectorInfo;
    }

    public void setConnectorInfo(AbstractConnectorInfo connectorInfo) {
       this.connectorInfo = connectorInfo;
    }

    public void setConnectorPoolingSupported(boolean supported) {
        isConnectorPoolingSupported = supported;
    }

    public void setConnectorPoolConfiguration(ObjectPoolConfiguration config) {
        connectorPoolConfiguration = config;
    }

    public void setConfigurationProperties(ConfigurationPropertiesImpl properties) {
        if (configurationProperties != null) {
            configurationProperties.setParent(null);
        }
        configurationProperties = properties;
        if (configurationProperties != null) {
            configurationProperties.setParent(this);
        }
    }


    public Map<Class<? extends APIOperation>, Integer> getTimeoutMap() {
        return timeoutMap;
    }

    public void setTimeoutMap(Map<Class<? extends APIOperation>, Integer> map) {
        timeoutMap = map;
    }

    public void setSupportedOperations(Set<Class<? extends APIOperation>> op) {
        supportedOperations = op;
    }

    public boolean isSupportedOperation(Class<? extends APIOperation> api) {
        return supportedOperations.contains(api);
    }

    // =======================================================================
    // Interface Methods
    // =======================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnectorPoolingSupported() {
        return isConnectorPoolingSupported;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectPoolConfiguration getConnectorPoolConfiguration() {
        if (connectorPoolConfiguration == null) {
            connectorPoolConfiguration = new ObjectPoolConfiguration();
        }
        return connectorPoolConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationPropertiesImpl getConfigurationProperties() {
        return configurationProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTimeout(Class<? extends APIOperation> operation) {
        Integer ret = this.timeoutMap.get(operation);
        if (ret == null) {
            // use the default..
            ret = APIOperation.NO_TIMEOUT;
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<? extends APIOperation>> getSupportedOperations() {
        return CollectionUtil.newReadOnlySet(supportedOperations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeout(Class<? extends APIOperation> operation,
            int timeout) {
        this.timeoutMap.put(operation, timeout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProducerBufferSize(int size) {
        this.bufferSize = size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getProducerBufferSize() {
        return this.bufferSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultsHandlerConfiguration getResultsHandlerConfiguration() {
        if (null == resultsHandlerConfiguration) {
            resultsHandlerConfiguration = new ResultsHandlerConfiguration();
        }
        return resultsHandlerConfiguration;
    }

    public void setResultsHandlerConfiguration(ResultsHandlerConfiguration config) {
        this.resultsHandlerConfiguration = config;
    }
}
