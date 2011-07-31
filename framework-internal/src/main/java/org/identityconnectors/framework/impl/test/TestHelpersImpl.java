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
package org.identityconnectors.framework.impl.test;

import java.util.HashMap;
import java.util.Map;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.common.FrameworkUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.ConnectorMessages;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.ConfigurationPropertiesImpl;
import org.identityconnectors.framework.impl.api.local.JavaClassProperties;
import org.identityconnectors.framework.impl.api.local.LocalConnectorInfoImpl;
import org.identityconnectors.framework.impl.api.local.operations.SearchImpl;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.test.common.spi.TestHelpersSpi;


public class TestHelpersImpl implements TestHelpersSpi {

    private static final Log _log = Log.getLog(TestHelpersImpl.class);
    
    /**
     * Method for convenient testing of local connectors. 
     */
    public APIConfiguration createTestConfiguration(Class<? extends Connector> clazz,
            Configuration config) {
        LocalConnectorInfoImpl info = new LocalConnectorInfoImpl();
        info.setConnectorConfigurationClass(config.getClass());
        info.setConnectorClass(clazz);
        info.setConnectorDisplayNameKey("DUMMY_DISPLAY_NAME");
        info.setConnectorKey(
               new ConnectorKey(clazz.getName()+".bundle",
                "1.0",
                clazz.getName()));
        info.setMessages(createDummyMessages());
        try {
            APIConfigurationImpl rv = new APIConfigurationImpl();
            rv.setConnectorPoolingSupported(
                    PoolableConnector.class.isAssignableFrom(clazz));
            ConfigurationPropertiesImpl properties =
                JavaClassProperties.createConfigurationProperties(config);
            rv.setConfigurationProperties(properties);
            rv.setConnectorInfo(info);
            rv.setSupportedOperations(
                    FrameworkUtil.getDefaultSupportedOperations(clazz));
            info.setDefaultAPIConfiguration(
                    rv);
            return rv;
        } catch (Exception e) {
            throw ConnectorException.wrap(e);
        }
    }        
    
    public void fillConfiguration(Configuration config, Map<String, ? extends Object> configData) {
        Map<String, Object> configDataCopy = new HashMap<String, Object>(configData);
        ConfigurationPropertiesImpl configProps =
            JavaClassProperties.createConfigurationProperties(config);
        for (String propName : configProps.getPropertyNames()) {
            // Remove the entry from the config map, so that at the end
            // the map only contains entries that were not assigned to a config property.
            Object value = configDataCopy.remove(propName);
            if (value != null) {
                configProps.setPropertyValue(propName, value);
            }
        }
        // The config map now contains entries that were not assigned to a config property.
        for (String propName : configDataCopy.keySet()) {
            _log.warn("Configuration property {0} does not exist!", propName);
        }
        JavaClassProperties.mergeIntoBean(configProps, config);
    }
        
    /**
     * Performs a raw, unfiltered search at the SPI level,
     * eliminating duplicates from the result set.
     * @param search The search SPI
     * @param oclass The object class - passed through to
     * connector so it may be null if the connecor
     * allowing it to be null. (This is convenient for
     * unit tests, but will not be the case in general)
     * @param filter The filter to search on
     * @param handler The result handler
     * @param options The options - may be null - will
     *  be cast to an empty OperationOptions
     */
    public void search(SearchOp<?> search,
            final ObjectClass oclass, 
            final Filter filter, 
            ResultsHandler handler,
            OperationOptions options) {
        if ( options == null ) {
            options = new OperationOptionsBuilder().build();
        }
        SearchImpl.rawSearch(search, oclass, filter, handler, options);
    }
    
    public ConnectorMessages createDummyMessages() {
        return new DummyConnectorMessages();
    }
    
    private static class DummyConnectorMessages implements ConnectorMessages {
        public String format(String key, String dflt, Object... args) {
            StringBuilder builder = new StringBuilder();
            builder.append(key);
            builder.append(": ");
            String sep = "";
            for (Object arg : args ) {
                builder.append(sep);
                builder.append(arg);
                sep=", ";
            }
            return builder.toString();
        }
    }

}
