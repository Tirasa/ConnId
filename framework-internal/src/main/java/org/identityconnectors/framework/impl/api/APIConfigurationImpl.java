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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.pooling.ObjectPoolConfiguration;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.operations.APIOperation;


public class APIConfigurationImpl implements APIConfiguration {

    // =======================================================================
    // Fields
    // =======================================================================
    /**
     * All configuration related to connector pooling.
     */
    private ObjectPoolConfiguration _connectorPoolConfiguration;
    
    private boolean _isConnectorPoolingSupported;
    
    private ConfigurationPropertiesImpl _configurationProperties;
           
    /**
     * Default size of the buffer.
     */
    private int _bufferSize = 100;
    
    /**
     * Map of timeout per operation.
     */
    private Map<Class<? extends APIOperation>, Integer> _timeoutMap 
    = new HashMap<Class<? extends APIOperation>, Integer>();

    /**
     * Set of supported operations;
     */
    private Set<Class<? extends APIOperation>> _supportedOperations; 
    
    
    /**
     * The connector info from which this was created. Not serialized in this 
     * object. Set when returned from the parent
     */
    private transient AbstractConnectorInfo _connectorInfo;
    
    // =======================================================================
    // Internal Methods
    // =======================================================================
   
    
    public AbstractConnectorInfo getConnectorInfo() {
        return _connectorInfo;
    }
    
    public void setConnectorInfo(AbstractConnectorInfo connectorInfo) {
       _connectorInfo = connectorInfo;
    }
    
    public void setConnectorPoolingSupported(boolean supported) {
        _isConnectorPoolingSupported = supported;
    }
    
    public void setConnectorPoolConfiguration(ObjectPoolConfiguration config) {
        _connectorPoolConfiguration = config;
    }
    
    public void setConfigurationProperties(ConfigurationPropertiesImpl properties) {
        if (_configurationProperties != null) {
            _configurationProperties.setParent(null);
        }
        _configurationProperties = properties;
        if (_configurationProperties != null) {
            _configurationProperties.setParent(this);
        }        
    }

    
    public Map<Class<? extends APIOperation>, Integer> getTimeoutMap() {
        return _timeoutMap;
    }
    
    public void setTimeoutMap(Map<Class<? extends APIOperation>, Integer> map) {
        _timeoutMap = map;
    }
    
    public void setSupportedOperations(Set<Class<? extends APIOperation>> op) {
        _supportedOperations = op;        
    }
    

    // =======================================================================
    // Interface Methods
    // =======================================================================

    /**
     * {@inheritDoc}
     */
    public boolean isConnectorPoolingSupported() {
        return _isConnectorPoolingSupported;
    }

    /**
     * {@inheritDoc}
     */
    public ObjectPoolConfiguration getConnectorPoolConfiguration() {
        if (_connectorPoolConfiguration == null) {
            _connectorPoolConfiguration = new ObjectPoolConfiguration();
        }
        return _connectorPoolConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    public ConfigurationPropertiesImpl getConfigurationProperties() {
        return _configurationProperties;
    }    

    /**
     * {@inheritDoc}
     */
    public int getTimeout(Class<? extends APIOperation> operation) {
        Integer ret = this._timeoutMap.get(operation);
        if (ret == null) {
            // use the default..
            ret = APIOperation.NO_TIMEOUT;
        }
        return ret;
    }
    
    /**
     * {@inheritDoc}
     */
    public Set<Class<? extends APIOperation>> getSupportedOperations() {
        return CollectionUtil.newReadOnlySet(_supportedOperations);
    }

    /**
     * {@inheritDoc}
     */
    public void setTimeout(Class<? extends APIOperation> operation, 
            int timeout) {
        this._timeoutMap.put(operation, timeout);
    }

    /**
     * {@inheritDoc}
     */
    public void setProducerBufferSize(int size) {
        this._bufferSize = size;
    }

    /**
     * {@inheritDoc}
     */
    public int getProducerBufferSize() {
        return this._bufferSize;
    }
}
