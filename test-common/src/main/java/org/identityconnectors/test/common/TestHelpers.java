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
package org.identityconnectors.test.common;

import java.lang.reflect.Method;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.ConnectorMessages;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.test.common.spi.TestHelpersSpi;

/**
 * Bag of utility methods useful to connector tests.
 */
public final class TestHelpers {

    private static final Object LOCK = new Object();

    private TestHelpers() {
    }

    /**
     * Method for convenient testing of local connectors.
     */
    public static APIConfiguration createTestConfiguration(
            Class<? extends Connector> clazz, Configuration config) {
        return getSpi().createTestConfiguration(clazz, config);
    }

    /**
     * Fills a configuration bean with data from the given map. The map
     * keys are configuration property names and the values are
     * configuration property values.
     * 
     * @param config the configuration bean.
     * @param configData the map with configuration data.
     */
    public static void fillConfiguration(Configuration config,
            Map<String, ? extends Object> configData) {
        getSpi().fillConfiguration(config, configData);
    }

    /**
     * Creates an dummy message catalog ideal for unit testing. All messages are
     * formatted as follows:
     * <p>
     * <code><i>message-key</i>: <i>arg0.toString()</i>, ..., <i>argn.toString</i></code>
     * 
     * @return A dummy message catalog.
     */
    public static ConnectorMessages createDummyMessages() {
        return getSpi().createDummyMessages();
    }

    public static List<ConnectorObject> searchToList(SearchApiOp search,
            ObjectClass oclass, Filter filter) {
        return searchToList(search, oclass, filter, null);
    }

    public static List<ConnectorObject> searchToList(SearchApiOp search,
            ObjectClass oclass, Filter filter, OperationOptions options) {
        ToListResultsHandler handler = new ToListResultsHandler();
        search.search(oclass, filter, handler, options);
        return handler.getObjects();
    }

    /**
     * Performs a raw, unfiltered search at the SPI level, eliminating
     * duplicates from the result set.
     * 
     * @param search
     *            The search SPI
     * @param oclass
     *            The object class - passed through to connector so it may be
     *            null if the connecor allowing it to be null. (This is
     *            convenient for unit tests, but will not be the case in
     *            general)
     * @param filter
     *            The filter to search on
     * @return The list of results.
     */
    public static List<ConnectorObject> searchToList(SearchOp<?> search,
            ObjectClass oclass, Filter filter) {
        return searchToList(search, oclass, filter, null);
    }

    /**
     * Performs a raw, unfiltered search at the SPI level, eliminating
     * duplicates from the result set.
     * 
     * @param search
     *            The search SPI
     * @param oclass
     *            The object class - passed through to connector so it may be
     *            null if the connecor allowing it to be null. (This is
     *            convenient for unit tests, but will not be the case in
     *            general)
     * @param filter
     *            The filter to search on
     * @param options
     *            The options - may be null - will be cast to an empty
     *            OperationOptions
     * @return The list of results.
     */
    public static List<ConnectorObject> searchToList(SearchOp<?> search,
            ObjectClass oclass, Filter filter, OperationOptions options) {
        ToListResultsHandler handler = new ToListResultsHandler();
        search(search, oclass, filter, handler, options);
        return handler.getObjects();
    }

    /**
     * Performs a raw, unfiltered search at the SPI level, eliminating
     * duplicates from the result set.
     * 
     * @param search
     *            The search SPI
     * @param oclass
     *            The object class - passed through to connector so it may be
     *            null if the connecor allowing it to be null. (This is
     *            convenient for unit tests, but will not be the case in
     *            general)
     * @param filter
     *            The filter to search on
     * @param handler
     *            The result handler
     * @param options
     *            The options - may be null - will be cast to an empty
     *            OperationOptions
     */
    public static void search(SearchOp<?> search, final ObjectClass oclass,
            final Filter filter, ResultsHandler handler,
            OperationOptions options) {
        getSpi().search(search, oclass, filter, handler, options);
    }

    // At some point we might make this pluggable, but for now, hard-code
    private static final String IMPL_NAME = "org.identityconnectors.framework.impl.test.TestHelpersImpl";

    private static TestHelpersSpi _instance;

    /**
     * Returns the instance of the SPI implementation.
     * 
     * @return The instance of the SPI implementation.
     */
    private static synchronized TestHelpersSpi getSpi() {
        if (_instance == null) {
            try {
                Class<?> clazz = Class.forName(IMPL_NAME);
                Object object = clazz.newInstance();
                _instance = TestHelpersSpi.class.cast(object);
            } catch (Exception e) {
                throw ConnectorException.wrap(e);
            }
        }
        return _instance;
    }

    private static final Map<String, PropertyBag> bags = new HashMap<String, PropertyBag>();




    
    /**
     * Loads Property bag for the specified class.
     * The properties are loaded as classpath resources using the class argument as root prefix.
     * Optional system property 'testConfig' is used to specify another configuration path for properties.
     * The following algorithm is used to load the properties in bag
     * <ul>
     *  <li><code>loader.getResource(prefix + "/config/config.groovy")</code></li>
     *  <li><code>loader.getResource(prefix + "/config/" + cfg + "/config.groovy") </code> optionally where cfg is passed configuration</li>
     *  <li> <code> loader.getResource(prefix + "/config-private/config.groovy") </<code> </li>
     *  <li> <code >loader.getResource(prefix + "/config-private/" + cfg + "/config.groovy") </code> optionally where cfg is passed configuration</li>
     * </ul>
     * Context classloader is used to load the resources.  
     * @param clazz Class which FQN is used as root prefix for loading of properties 
     * @return Bag of properties for specified class and optionally passed configuration
     * @throws IllegalStateException if context classloader is null
     */
    public static PropertyBag getProperties(Class<?> clazz) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if(loader == null){
            throw new IllegalStateException("Thread.currentThread().getContextClassLoader() is null, please set context ClassLoader");
        }
        return getProperties(clazz, loader);
    }

    static Map<?, ?> loadGroovyConfigFile(URL url) {
        try {
            Class<?> slurper = Class.forName("groovy.util.ConfigSlurper");
            Class<?> configObject = Class.forName("groovy.util.ConfigObject");
            Object slurpInstance = slurper.newInstance();
            Method parse = slurper.getMethod("parse", URL.class);
            Object config = parse.invoke(slurpInstance, url);
            Method toProps = configObject.getMethod("flatten");
            Object result = toProps.invoke(config);
            return (Map<?, ?>) result;
        } catch (Exception e) {
            throw new ConnectorException(MessageFormat.format("Could not load Groovy config file ''{0}''", url), e);
        }
    }

    static PropertyBag getProperties(Class<?> clazz, ClassLoader loader) {
        synchronized (LOCK) {
            PropertyBag bag = bags.get(clazz.getName());
            if (bag == null) {
                bag = loadConnectorConfigurationAsResource(clazz.getName(), loader);
                bags.put(clazz.getName(), bag);
            }
            return bag;
        }
    }

    static PropertyBag loadConnectorConfigurationAsResource(String prefix, ClassLoader loader) {
        Map<String, Object> ret = new HashMap<String, Object>();
        String cfg = System.getProperty("testConfig", null);
        //common public config file
        URL url = loader.getResource(prefix + "/config/config.groovy");
        if (url != null) {
            appendProperties(ret, loadGroovyConfigFile(url));
        }
        if (StringUtil.isNotBlank(cfg) && !"default".equals(cfg)) {
            //public config file specific for one particular configuration 
            url = loader.getResource(prefix + "/config/" + cfg + "/config.groovy");
            if (url != null) {
                appendProperties(ret, loadGroovyConfigFile(url));
            }
        }
        //common private config file
        url = loader.getResource(prefix + "/config-private/config.groovy");
        if (url != null) {
            appendProperties(ret, loadGroovyConfigFile(url));
        }
        if (StringUtil.isNotBlank(cfg) && !"default".equals(cfg)) {
            //private config file specific for one particular configuration 
            url = loader.getResource(prefix + "/config-private/" + cfg + "/config.groovy");
            if (url != null) {
                appendProperties(ret, loadGroovyConfigFile(url));
            }
        }
        return new PropertyBag(ret);
    }

	static void appendProperties(Map<String, Object> ret, Map<?, ?> props) {
		if (props != null) {
			for (Entry<?, ?> entry : props.entrySet()) {
				Object key = entry.getKey();
				if (key instanceof String) {
					ret.put((String) key, entry.getValue());
				}
				else{
					throw new IllegalStateException("Entry in read properties has not string key : " + entry);
				}
			}
		}
	}

}
