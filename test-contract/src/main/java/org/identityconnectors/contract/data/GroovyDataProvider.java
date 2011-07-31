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
package org.identityconnectors.contract.data;

import static org.junit.Assert.assertNotNull;
import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.contract.data.groovy.Get;
import org.identityconnectors.contract.data.groovy.Lazy;
import org.identityconnectors.contract.data.groovy.Random;
import org.identityconnectors.contract.exceptions.ObjectNotFoundException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.test.common.TestHelpers;
import org.junit.Assert;
/**
 * <p>
 * Default implementation of {@link DataProvider}. It uses ConfigSlurper from
 * Groovy to parse the property file.
 * The groovy files are read as classpath resources using following paths : 
     * <ul>
     *  <li><code>loader.getResource(prefix + "/config/config.groovy")</code></li>
     *  <li><code>loader.getResource(prefix + "/config/" + cfg + "/config.groovy") </code> optionally where cfg is passed configuration</li>
     *  <li> <code> loader.getResource(prefix + "/config-private/config.groovy") </<code> </li>
     *  <li> <code >loader.getResource(prefix + "/config-private/" + cfg + "/config.groovy") </code> optionally where cfg is passed configuration</li>
     * </ul>
   where prefix is FQN of your connector set as "connectorName" system property.
 * Note: If two property files contain the same property name, the value from
 * the latter file the list <b>overrides</b> the others. I.e. the last file
 * from the list has the greatest chance to propagate its values to the final
 * configuration.
 * </p>
 * 
 * <p>
 * <code>Lazy.random("####")</code> is used for generating random strings, in
 * case numeric object is needed, use for instance
 * <code>Lazy.random("####", Long.class)</code> to get a Long object with
 * random value.
 * </p>
 * 
 * <p>
 * <strong>Snapshot generation to output file</strong> -- this feature is
 * implemented by method {@link GroovyDataProvider#flatten(Object)}. Snapshot
 * generating works in one direction, but the snapshot itself cannot be directly 
 * used as an input to next testing.
 * </p>
 * <p>
 * <strong>Snapshots -- usage:</strong> add switch <code>-Dtest.parameters.outFile=generated.properties</code>
 * as an ANT parameter. The result snapshot file will be included in the connector's directory.
 * </p>
 * <p>
 * Note: snapshots for now support basic types such as Lazy, String. Other objects
 * will be converted with toString() method to the output.
 * </p>
 * <p>
 * <strong>
 * Snapshots of queried properties -- usage:
 * </strong>
 * add switch <code>-Dtest.parameters.outQueriedFile=dumpedq.properties</code>
 * as an ANT parameter. The result snapshot file will be included in the connector's directory.
 * </p>
 * <p>
 * <strong>default values</strong> -- these values reside in file bootstrap.groovy. 
 * When the property foo.bar.boo is queried the following queries are executed:
 * <pre>
 * 1) foo.bar.boo
 * 2) bar.boo
 * 3) boo
 * </pre>
 * In case none of these queries succeed, the default value is used based on the type of the query.
 * </p>
 * <p>
 * <strong>isMultivalue</strong> boolean property -- is passed in get(...) methods 
 * of GroovyDataProvider. It has influence on default values generated, when property is missing.
 * </p>
 * 
 * @author David Adam
 * @author Zdenek Louzensky
 */
public class GroovyDataProvider implements DataProvider {
    
    private static final int SINGLE_VALUE_MARKER = -1;
    private static final String ARRAY_MARKER = "array";
    static final String PROPERTY_SEPARATOR = ".";
    
    /** boostrap.groovy contains default values that are returned when the property is not found */
    private static final String BOOTSTRAP_FILE_NAME = "bootstrap.groovy";
     
    /** prefix of default values that are multi */
    public static final String MULTI_VALUE_TYPE_PREFIX = "multi";
    
    /** holds the parsed config file */
    private ConfigObject configObject;

    /** cache for resolved values */
    private Map<String, Object> cache = new HashMap<String, Object>();

    private final ConfigSlurper cs = new ConfigSlurper();

    private static final Log LOG = Log.getLog(GroovyDataProvider.class);

    
    /* **** for snapshot generating **** */
    /** command line switch for snapshots */
    private final String PARAM_PROPERTY_OUT_FILE = "test.parameters.outFile";
    /** command line switch for creating queried properties' dump */
    private final String PARAM_QUERIED_PROPERTY_OUT_FILE = "test.parameters.outQueriedFile";
    /** buffer for queried properties log */
    private StringBuffer dumpBuffer = null;
    /** buffer for queried properties -- that were not found -- log */
    private StringBuffer dumpBufferNotFound = null;
    /** default values generated for */
    private StringBuffer dumpBufferDefaultVal = null;
    /** output file for concatenated snapshots */
    private File _propertyOutFile = null;
    /** output file for queried properties dump */
    private File _queriedPropsOutFile = null;
    static final String ASSIGNMENT_MARK = "=";

    private final String FOUND_MSG = "found";
    /** Turn on debugging prefixes in parsing. Output: System.out */
    private final boolean DEBUG_ON = false;
    private final String EMPTY_PREFIX = "";
    

    /**
     * default constructor
     */
    public GroovyDataProvider() {
        this(System.getProperty("connectorName"));
    }
    
    public GroovyDataProvider(String connectorName){
        if (StringUtil.isBlank(connectorName)) {
            throw new IllegalArgumentException(
                    "To run contract tests, you must specify valid [connectorName] system property with the value equal to FQN of your connector class, or use GroovyDataProvider(String connectorName) constructor");

        }
        initSnapshot();
        initQueriedPropsDump();
        
        // init
        configObject = doBootstrap();
        ConfigObject projectConfig = GroovyConfigReader.loadResourceConfiguration(connectorName, getClass().getClassLoader());
        configObject = mergeConfigObjects(configObject, projectConfig);
        
        checkJarDependencies(this, this.getClass().getClassLoader());
    }


    /**
     * check the presence of expected JAR's on the classpath
     * <p>
     * The test configuration file should contain a definition of required JARs.
     * The key of the map is the classname that's needed, the value is
     * information included in the error message (supposed to be the jar's
     * name).
     * <p>
     * It could be example:
     * <p>
     * <code>testsuite.requiredClasses = [ 'com.mysql.jdbc.Driver' : 'Connector/J
     * 5.0.8 (mysql-connector-java-5.0.8-bin.jar)' ]</code>
     * <p>
     * where 'com.mysql.jdbc.Driver' is the awaited class, and 'Connector/J...'
     * is the information message describing the JAR, where the class resides.
     */
    private static void checkJarDependencies(DataProvider dp, ClassLoader classLoader) {
        final String PROP_REQUIRED_CLASSES = "requiredClasses";
        Object o = null;
        try {
            o = dp.getTestSuiteAttribute(PROP_REQUIRED_CLASSES);
        } catch (ObjectNotFoundException ex) {
            // if property testsuite.requiredClasses is undefined skip checking JARs.
            return;
        }
        if (o instanceof Map<?,?>) {
            @SuppressWarnings("unchecked")
            Map<String, String> map = (Map<String , String>) o;
            for (Map.Entry<String , String> entry : map.entrySet()) {
                try {
                    Class.forName(entry.getKey(), false, classLoader);
                } catch (ClassNotFoundException e) {
                    Assert.fail(String.format("Missing library from classpath: '%s'", entry.getValue()));
                }
            }
        }
    }

    private void initQueriedPropsDump() {
        // get snapshot output file, if provided
        String pOut = System.getProperty(PARAM_QUERIED_PROPERTY_OUT_FILE);
        if (StringUtil.isNotBlank(pOut)) {
            try {
                _queriedPropsOutFile = new File(pOut);
                if (!_queriedPropsOutFile.exists()) {
                    _queriedPropsOutFile.createNewFile();
                }
                if (!_queriedPropsOutFile.canWrite()) {
                    _queriedPropsOutFile = null;
                    LOG.warn("Unable to write to ''{0}'' file, the test parameters will not be stored", pOut);
                } else {
                    LOG.info("Storing parameter values to ''{0}'', you can rerun the test with the same parameters later", pOut);
                }
            } catch (IOException iOException) {
                LOG.warn("Unable to create ''{0}'' file, the test parameters will not be stored", pOut);
            }
        }
        
        this.dumpBuffer = new StringBuffer();
        this.dumpBufferNotFound = new StringBuffer();
        this.dumpBufferDefaultVal = new StringBuffer();
    }

    private void initSnapshot() {
        // get snapshot output file, if provided
        String pOut = System.getProperty(PARAM_PROPERTY_OUT_FILE);
        if (StringUtil.isNotBlank(pOut)) {
            try {
                _propertyOutFile = new File(pOut);
                if (!_propertyOutFile.exists()) {
                    _propertyOutFile.createNewFile();
                }
                if (!_propertyOutFile.canWrite()) {
                    _propertyOutFile = null;
                    LOG.warn("Unable to write to ''{0}'' file, the test parameters will not be stored", pOut);
                } else {
                    LOG.info("Storing parameter values to ''{0}'', you can rerun the test with the same parameters later", pOut);
                }
            } catch (IOException iOException) {
                LOG.warn("Unable to create ''{0}'' file, the test parameters will not be stored", pOut);
            }
        }
    }

    /**
     * Constructor for JUnit Testing purposes only. Do not use it normally.
     */
    GroovyDataProvider(URL configURL) {
        configObject = doBootstrap();

        // parse the configuration file once
        ConfigObject highPriorityCO = cs.parse(configURL);
        configObject = mergeConfigObjects(configObject, highPriorityCO);
    }

    /** load the bootstrap configuration */
    private ConfigObject doBootstrap() {        
        URL url = getClass().getClassLoader().getResource(BOOTSTRAP_FILE_NAME);
        String msg = String.format("Missing bootstrap file: %s. (Hint: copy " +
        		"framework/test-contract/src/bootstrap.groovy to folder framework/test-contract/build)", BOOTSTRAP_FILE_NAME);
        Assert.assertNotNull(msg, url);
        return cs.parse(url);
    }


    /**
     * merge two config objects. If both of config objects contian the same 
     * property key, then the value of <code>highPriorityCO</code> is propagated
     * to the result.
     * @param lowPriorityCO
     * @param highPriorityCO
     * @return the merged version of two config objects.
     */
    static ConfigObject mergeConfigObjects(ConfigObject lowPriorityCO,
            ConfigObject highPriorityCO) {
        return (ConfigObject) lowPriorityCO.merge(highPriorityCO);
    }

    /**
     * 
     * Main get method. Property lookup starts here.
     * 
     */
    public Object get(String name, String type, boolean useDefault) {
        Object o = null;
        /** indicates if default value used */
        boolean isDefaultValue = false;
        /** indicates if the property was properly found */
        boolean isFound = true;

        try {
            o = propertyRecursiveGet(name);
        } catch (ObjectNotFoundException onfe) {
            // What to do in case of missing property value:
            if (useDefault) {
                isDefaultValue = true;
                // generate a default value
                o = propertyRecursiveGet(type);
            } else {
                isFound = false;
                if (useDefault) {
                    throw new ObjectNotFoundException("Missing property definition: " + name
                            + ", data type: " + type);
                } else {
                    throw new ObjectNotFoundException("Missing property definition: " + name);
                }
            }
        } finally {
            if (_queriedPropsOutFile != null) {
                logQueriedProperties(o, name, type, isDefaultValue, isFound);
            }
        }

        // resolve o.n.f.e.
        if (o instanceof ObjectNotFoundException) {
            throw (ObjectNotFoundException) o;
        }
        
        // cache resolved value
        cache.put(name, o);
        return o;
    }

    /**
     * dump the current property query results into local buffer
     * @param queriedObject the object value that was returned from query
     * @param name the name of property that was queried
     * @param type 
     * @param isDefaultValue if default value was returned
     * @param isFound if it was succesfully found
     */
    private void logQueriedProperties(Object queriedObject, String name, String type,
            boolean isDefaultValue, boolean isFound) {
        String msg = "name: '%s' type: '%s' defaultReturned: '%s' %s: '%s'";
        String appendInfo = String.format(msg, name, type, 
                Boolean.toString(isDefaultValue), FOUND_MSG, Boolean.toString(isFound))
                + ((queriedObject != null)? (" value: " + flatten(queriedObject)):"") + "\n";
        this.dumpBuffer.append(appendInfo);
        if (isFound == false) {
            this.dumpBufferNotFound.append(appendInfo);
        }
        if (isDefaultValue == true) {
            this.dumpBufferDefaultVal.append(appendInfo);
        }
    }

    /**
     * try to resolve the property's value
     * 
     * @param name
     * @return
     */
    private Object propertyRecursiveGet(String name)  {
        Object response = null;
        
        if (!cache.containsKey(name)) {
            try {
    
                // get the property for given name
                // (in case property is not found, ObjectNotFoundException will be
                // thrown.)
                response = configObjectRecursiveGet(name, this.configObject);
    
            } catch (ObjectNotFoundException onfe) {
                // we did not found the property for given name, try to search it
                // recursively
                // by deleting the first prefix
                int separatorIndex = name.indexOf(PROPERTY_SEPARATOR, 0);
    
                if (separatorIndex != SINGLE_VALUE_MARKER) {
                    separatorIndex++;
                    if (separatorIndex < name.length()) {
                        return propertyRecursiveGet(name.substring(separatorIndex));
                    }
                } else {
                    throw new ObjectNotFoundException(
                            "Can't find object for key:  " + name);
                }// fi
            }// catch
        }
        else {
            response = cache.get(name);
        }

        return response;
    }

    /**
     * contains key functionality for acquiring properties with hierarchical
     * names (e.g. foo.bar.spam) from ConfigObject
     * 
     * @param name
     *            property name
     * @param co
     *            configuration model, that contains all the property key/value
     *            pairs
     * @return
     * @throws ObjectNotFoundException
     */
    private Object configObjectRecursiveGet(String name, ConfigObject co) {
        int dotIndex = name.indexOf(PROPERTY_SEPARATOR);
        if (dotIndex >= 0) {
            String currentNamePart = name.substring(0, dotIndex);

            /*
             * request the property name from parsed config file
             */
            Object o = configObjectGet(co, currentNamePart);

            if (o instanceof ConfigObject) {
                // recursively resolve the hierarchical names (containing
                // multiple dots.
                return configObjectRecursiveGet(name.substring(dotIndex + 1), (ConfigObject) o);
            } else {
                final String MSG = "Unexpected object instance. Searching property: '%s', found value: '%s', expected value is ConfigObject. Please check that property '%s' is defined - it can collide with attribute value definition.";
                Assert.fail(String.format(MSG, name, o.toString(), name));
                return null;
            }// fi inner

        } else {
            /*
             * request the property name from parsed config file
             */
            return configObjectGet(co, name);
        }
    }

    /**
     * 
     * @param co
     *            current config object which is queried
     * @param currentNamePart
     *            the queried property name
     * @return the value for given property name
     * @throws ObjectNotFoundException
     */
    private Object configObjectGet(ConfigObject co, String currentNamePart) {

        /*
         * get the property value
         */
        Object result = co.getProperty(currentNamePart);

        if (result instanceof ConfigObject) {
            // try if property value is empty
            ConfigObject coResult = (ConfigObject) result;
            if (coResult.size() == 0) {
                throw new ObjectNotFoundException();
            }
        } else {
            result = resolvePropObject(result);
        }// fi
        return result;
    }

    /**
     * Resolve the special types of property object (right side of assigment operator). 
     * There are two types supported:
     * <ul>
     * <li>{@link List}</li>
     * <li>{@link Lazy}</li>
     * </ul>
     * 
     * @param o
     * @return the resolved property object
     */
    private Object resolvePropObject(Object o) {
        Object resolved = o;

        if (o instanceof Lazy) {
            Lazy lazy = (Lazy) o;

            resolved = resolveLazy(lazy);
        } else if (o instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) o;
            resolved = resolveList(list);
        } else if (o instanceof Map<?,?>) {
            @SuppressWarnings("unchecked")
            Map<?,Object> map = (Map<?,Object>) o;
            resolved = resolveMap(map);
        }
        return resolved;
    }

    /**
     * Method that resolves all Lazy values within the given map. Resolving 
     * works recursively for nested Maps also.
     * @param map
     * @return
     */
    private Map<?, Object> resolveMap(Map<?, Object> map) {
        Map<?, Object> localMap = map;
        for (Map.Entry<?, Object> pairKV : localMap.entrySet()) {
            /** value */
            Object object = pairKV.getValue();
            
            if (object instanceof Lazy) {
                Lazy lazyO = (Lazy) object;
                Object resolvedObj = resolveLazy(lazyO);
                pairKV.setValue(resolvedObj);
            } else if (object instanceof Map<?,?>) {
                // recursively resolve attributes in nested lists
                @SuppressWarnings("unchecked")
                Map<?, Object> arg = (Map<?, Object>) object;
                Map<?, Object> resolvedMap = resolveMap(arg);
                pairKV.setValue(resolvedMap);
            }
        }// for list
        return localMap;
    }

    /**
     * Method that resolves all Lazy values within the given list. Resolving
     * works recursively for nested lists also.
     * 
     * @param list
     * @return the list with resolved values
     */
    private List<Object> resolveList(List<Object> list) {
        List<Object> result = new ArrayList<Object>();
        for (Object object : list) {
            if (object instanceof Lazy) {
                Lazy lazyO = (Lazy) object;
                Object resolvedObj = resolveLazy(lazyO);
                result.add(resolvedObj);
            } else if (object instanceof List<?>) {
                // recursively resolve attributes in nested lists
                @SuppressWarnings("unchecked") // because of list type cast
                List<Object> arg = (List<Object>) object;
                List<Object> resolvedList = resolveList(arg);
                result.add(resolvedList);
            } else {
                result.add(object);
            }
        }
        return result;
    }

    /**
     * Resolve lazy initialization objects.
     * @param lazy
     * @return the resolved up to date values
     */
    private Object resolveLazy(Lazy lazy) {
        Object value = lazy.getValue();
        Object resolvedValue = null;
        if (value != null) {
            if (value instanceof Lazy) {
                value = resolveLazy((Lazy) value);
            }
            if (lazy instanceof Get) {
                Assert.assertTrue(value instanceof String);
                resolvedValue = get((String)value, null, false);
            } else if (lazy instanceof Random) {
                Assert.assertTrue(value instanceof String);
                Random rnd = (Random) lazy;
                resolvedValue = rnd.generate();
            }
        }

        if (!lazy.getSuccessors().isEmpty()) {
            return concatenate(resolvedValue, lazy.getSuccessors());
        } else {
            return resolvedValue;
        }
    }

    private String concatenate(Object value, List<Object> successors) {
        StringBuffer sb = new StringBuffer();
        if (value != null) {
            sb.append(value.toString());
        }
        for (Object o : successors) {
            if (o instanceof String) {
                sb.append((String) o);
            } else if (o instanceof Lazy) {
                Object resolved = resolveLazy((Lazy) o);
                sb.append(resolved.toString());
            }
        }

        return sb.toString();
    }

    /* ************************ interface DataProvider ********************** */

    /**
     * {@inheritDoc}
     */
    public Object get(Class<?> dataTypeName, String name, String componentName,
            int sequenceNumber, boolean isMultivalue)  {
        // put the parameters in the Map ... this will fail if called
        // recursively

        String shortTypeName = getShortTypeName(dataTypeName);
        // for multi values add the multi prefix when searching for default value.
        if (isMultivalue) {
            String tmp = String.format("multi%s%s", PROPERTY_SEPARATOR, shortTypeName);
            shortTypeName = tmp;
        }
        
        Assert.assertFalse(cache.keySet().contains("param.sequenceNumber"));
        Assert.assertFalse(cache.keySet().contains("param.componentName"));
        Assert.assertFalse(cache.keySet().contains("param.name"));
        Assert.assertFalse(cache.keySet().contains("param.dataTypeName"));

        StringBuffer sbPath = new StringBuffer();
        if (sequenceNumber != SINGLE_VALUE_MARKER) {
            sbPath.append("i");// sequence marker e.g.: i1, i2, i3 ...
            sbPath.append(sequenceNumber);
            sbPath.append(PROPERTY_SEPARATOR);
            cache.put("param.sequenceNumber", "i" + String.valueOf(sequenceNumber));
        }

        sbPath.append(componentName);
        sbPath.append(".");
        sbPath.append(name);
        LOG.info("getting data for ''{0}'', type: ''{1}''", sbPath,
                dataTypeName);
        
        cache.put("param.componentName", componentName);
        cache.put("param.name", name);
        cache.put("param.dataTypeName", shortTypeName);

        try {

            // call get to resolve the property value
            Object obj = get(sbPath.toString(), shortTypeName, true);

            LOG.info("Fully resolved ''{0}'' to value ''{1}''", sbPath
                    .toString(), obj);
            return obj;

        } catch (ObjectNotFoundException ex) {
            LOG.info("Unable to find data for ''{0}''", sbPath.toString());
            throw ex;
        } catch (Exception ex) {
            LOG.error(ex, "Error occured while resolving property ''{0}''", sbPath.toString());
        } finally {
            cache.remove("param.dataTypeName");
            cache.remove("param.name");
            cache.remove("param.componentName");
            cache.remove("param.sequenceNumber");

            Assert.assertFalse(cache.keySet().contains("param.sequenceNumber"));
            Assert.assertFalse(cache.keySet().contains("param.componentName"));
            Assert.assertFalse(cache.keySet().contains("param.name"));
            Assert.assertFalse(cache.keySet().contains("param.dataTypeName"));
        }
        // found nothing, so return nothing
        throw new ObjectNotFoundException("Can't find object for key:  " + sbPath.toString());

    }

    /**
     * {@inheritDoc}
     */
    public Object get(Class<?> dataTypeName, String name, String componentName) {

        return get(dataTypeName, name, componentName, SINGLE_VALUE_MARKER, false);
    }

    /**
     * {@inheritDoc}
     */
    public String getString(String name, String componentName,
            int sequenceNumber)  {
        return (String) get(String.class, name, componentName,
                sequenceNumber, false);
    }

    /**
     * {@inheritDoc}
     */
    public String getString(String name, String componentName) {
        return (String) get(String.class, name, componentName);
    }

    /**
     * {@inheritDoc}
     */
    public Object getTestSuiteAttribute(String propName) {

        return get("testsuite." + propName, null, false);
    }

    /**
     * {@inheritDoc}
     */
    public Object getTestSuiteAttribute(String propName, String testName)  {
        return get("testsuite." + testName + "." + propName, null, false);
    }

    /**
     * {@inheritDoc}
     */
    public Object getConnectorAttribute(String propName) {

        return get("connector." + propName, null, false);
    }
    
    /**
     * {@inheritDoc}
     */
    public Object get(String name) {
        Object result = get(name, null, false);
        if (result instanceof Map<?,?>) {
            @SuppressWarnings("unchecked")
            Map<?, Object> map = (Map<?, Object>) result;
            result = resolveMap(map);
        }
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public Object generate(String pattern, Class<?> clazz) {
        return RandomGenerator.generate(pattern, clazz);
    }

    /**
     * {@inheritDoc}
     */
    public Object generate(String pattern) {
        return RandomGenerator.generate(pattern);
    }

    /** 
     * {@inheritDoc}
     */
    public Object get(String name, int sequenceNumber) {
        String resolvedName = String.format("i%s%s%s", sequenceNumber, PROPERTY_SEPARATOR, name);

        return get(resolvedName);
    }

    /* ************** ADDITIONAL PROPERTY UTILS ******************* */
    /**
     * @param setName
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getPropertyMap(final String setName) {
        Map<String, Object> propMap = (Map<String, Object>) this
                .get(setName);
        return propMap;
    }

    /**
     * @param propertySetName
     * @return The set <CODE>Set<Attribute></CODE> of attributes
     */
    public Set<Attribute> getAttributeSet(final String propertySetName) {
        Map<String, Object> propMap = getPropertyMap(propertySetName);
        assertNotNull(propMap);
        Set<Attribute> attrSet = new LinkedHashSet<Attribute>();
        for (Entry<String, Object> entry : propMap.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue(); 
            if (value.getClass().isArray()) {
                Object[] array = (Object[]) value;
                List<?> list = Arrays.asList(array);
                attrSet.add(AttributeBuilder.build(key, list));
            } else if (value instanceof Collection<?>) {
                attrSet.add(AttributeBuilder.build(key, (Collection<?>) value));
            } else {
                attrSet.add(AttributeBuilder.build(key, value));
            }
        }
        return attrSet;
    }

    /**
     * @param configName
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws InvocationTargetException 
     * @throws NoSuchMethodException 
     * @throws SecurityException 
     */
    public void loadConfiguration(final String configName, Configuration cfg) {
        Map<String, ? extends Object> propMap = getPropertyMap(configName);
        assertNotNull(propMap);
        TestHelpers.fillConfiguration(cfg, propMap);
    }
    /* ************** AUXILIARY METHODS *********************** */
    /**
     * gets short name for the type, eg. java.lang.String returns string
     * 
     * @param Type
     *            Name
     * @return Short Name
     */
    static String getShortTypeName(Class<?> dataType) {
        /*
         * in case of arrays "datatype[]" is returned
         */
        String shortName = dataType.getSimpleName();
        final boolean isArray = dataType.isArray();
        
        if (isArray) {
            if (shortName.length() > 2) {
                String tmp = shortName.replace("[]", ARRAY_MARKER);
                shortName = tmp;
            } else {
                LOG.warn("Can't get short type for ''{0}'' (missing array type)", dataType.getName());
            }
        }

        return String.format("T%s", shortName.toLowerCase());
    }

    /* **************** SNAPSHOT GENERATOR METHODS **************** */
    /**
     * writes key, value to _propertyOutFile
     * @return the flattened properties as a String (only for test use)
     */
    Object writeDataToFile() {
        // do nothing if not having out file
        if (_propertyOutFile == null) {
            return null;
        }
        
        // parse configuration information to flatten
        String result = flatten(configObject);
        
        FileWriter fw = null;
        try {
            fw = new FileWriter(_propertyOutFile, true);
            fw.append("\n\n\n ============================ NEW TEST ==================== \n\n\n");
            fw.append(result);
            fw.close();
        } catch (IOException e) {
            LOG.warn("Writing to contract test property out file failed ''{0}''", _propertyOutFile.getAbsolutePath());
        }
        
        return result;
    }
    
    /**
     * save the dump into its file
     */
    private String writeQueriedDumpToFile() {
        // do nothing if not having out file
        if (_queriedPropsOutFile == null) {
            return null;
        }
        
        FileWriter fw = null;
        try {
            fw = new FileWriter(_queriedPropsOutFile, true);
            fw.append("<dumpSummary>\n");
            // MISSING PROPS
            fw.append("  <missingProperties>\n");
            String notFoundPropsList = this.dumpBufferNotFound.toString();
            if (StringUtil.isBlank(notFoundPropsList)) {
                fw.append("every property was found\n");
            } else {
                fw.append("The following properties WERE NOT FOUND:\n");
            }//fi
            fw.append(this.dumpBufferNotFound.toString() + "\n");
            fw.append("  </missingProperties>\n");
            // PROPS WITH DEFAULT VALUE
            fw.append("  <defaultValueGeneratedForProperties>\n");
            if (StringUtil.isBlank(this.dumpBufferDefaultVal.toString())) {
                fw.append("no default value was used.");
            } else {
                fw.append("the following default values were used.");
            }
            fw.append(this.dumpBufferDefaultVal.toString() + "\n");
            fw.append("  </defaultValueGeneratedForProperties>\n");
            fw.append("</dumpSummary> \n\n\n");
            
            fw.append("\n\n\n ============================ NEW TEST ==================== \n\n\n");
            fw.append(this.dumpBuffer.toString() + "\n");
            fw.close();
        } catch (IOException e) {
            LOG.warn("Writing to contract test property out file failed ''{0}''", _queriedPropsOutFile.getAbsolutePath());
        }
        
        LOG.info("Dump file of queried properties written to: ''{0}''", _queriedPropsOutFile.getAbsolutePath());
        
        return this.dumpBuffer.toString();
    }

    /**
     * <strong>Starting point</strong> of flattening methods.
     * 
     * @param obj
     *            the object to flatten
     * @param choice
     *            if there is qoutation needed in the output string
     * @param prefix
     *            is the previous part of property query, e.g. in case of query
     *            for foo.bar.boo, when we are flattening bar, prefix contains
     *            foo.
     */
    private String flatten(Object obj, Chooser choice, String prefix) {
        String svalue = null;

        if (obj instanceof ConfigObject) {

            svalue = flattenCO(obj, prefix);

        } else if (obj instanceof Map<?,?>) {

            svalue = flattenMap(obj);

        } else if (obj instanceof List<?>) {

            svalue = flattenList(obj);

        } 
        else if (obj instanceof Lazy) {
            Object resolvedObj = flattenLazy((Lazy) obj, prefix);
            svalue = quoteLazyIfNeeded(resolvedObj.toString());
        }else {
            // resolve as "Object" and use quotes, if needed
            /* simply print out a string for all types of objects, that are not recognized */
            String output = (obj == null) ? "null" : obj.toString();
            switch (choice) {
            case QUOTED:
                svalue = String.format("\"%s\"", output);
                break;
            case NOT_QUOTED:
                svalue = output;
                break;
            }
        }

        return svalue;
    }
    
    /**
     * create a snapshot of values recursively
     * 
     * @param obj
     *            an object to flatten
     * @return string representation
     * 
     * @see {@link GroovyDataProvider#flatten(Object, Chooser, String)}
     */
    private String flatten(Object obj) {
        return flatten(obj, Chooser.QUOTED, EMPTY_PREFIX);
    }
    
    /** @see {@link GroovyDataProvider#flatten(Object, Chooser, String)} */
    private String flatten(Object obj, String prefix) {
        return flatten(obj, Chooser.QUOTED, prefix);
    }

    
    /**
     * if the String starts with Lazy.random... there is no quotation needed, 
     * otherwise quote it.
     * 
     * @param string
     * @return the correctly quoted string
     */
    private String quoteLazyIfNeeded(String string) {
        if (!string.startsWith("Lazy")) { // FIXME dependence on GroovyDataProvider Lazy loading method name
            return String.format("\"%s\"", string);
        }
        return string;
    }

    private Object flattenLazy(Lazy lazy, String prefix) {
        Object value = lazy.getValue();
        Object resolvedValue = null;
        if (value != null) {
            if (value instanceof Lazy) {
                value = flattenLazy((Lazy) value, prefix);
            }
            if (lazy instanceof Get) {
                Assert.assertTrue(value instanceof String);
                resolvedValue = "Lazy.get(\"" + value + "\")";//get((String)value, null, false);
            } else if (lazy instanceof Random) {
                Random randomLazy = (Random) lazy;
                // IF there is the queried value in cache, use it. 
                // OTHERWISE put Lazy.random("originalPattern", typeArgument);
                if (cache.containsKey(prefix)) {
                    //use cached value:
                    resolvedValue = cache.get(prefix).toString();
                } else {
                    //System.out.println("no key: " + prefix + " /In cache");
                    resolvedValue = "Lazy.random(\"" + randomLazy.getValue() + "\", " + randomLazy.getClazz().getName() + ")";
                }
            }
        }

        if (!lazy.getSuccessors().isEmpty()) {
            return concatenate(resolvedValue, lazy.getSuccessors());
        } else {
            return resolvedValue;
        }
    }

    private String flattenCO(Object obj, String prefix) {
        String svalue = null;
        StringBuilder sb = new StringBuilder();
        boolean first = false;
        @SuppressWarnings("unchecked")
        Map<Object, Object> objMap = (Map<Object, Object>) obj;

        for (Map.Entry<Object, Object> entry : objMap.entrySet()) {
            /*
             * entry is made of _key_ and _value_ pair
             */
            // #1 _value_ part of the entry:
            String value = flatten(entry.getValue(), concatToPrefix(prefix, entry.getKey()));

            // if there are nested maps (representing hierarchical names,
            // e.g. foo.bar.laa):
            if (entry.getValue() instanceof ConfigObject) {
                // #2 _key_ part of the entry:
                String key = null;
                if (first) {
                    // if first, no quotes are needed:
                    key = debugStr("FST|")
                            + flatten(entry.getKey(), Chooser.NOT_QUOTED, concatToPrefix(prefix, entry.getKey()));
                } else {
                    key = debugStr("MID|") + flatten(entry.getKey(), concatToPrefix(prefix, entry.getKey()));
                }// fi (first)
                sb.append(String.format("%s%s%s", key, PROPERTY_SEPARATOR, value));

            } else {// fi (entry.getValue())
                // no nested property names left (no foo.bar.laa)
                String key = null;

                if (prefix.equals(EMPTY_PREFIX)) {
                    key = debugStr("LSTinSNGL|") + flatten(entry.getKey(), Chooser.NOT_QUOTED, prefix);
                } else {
                    key = debugStr("LSTinMULT|") + flatten(entry.getKey(), concatToPrefix(prefix, entry.getKey()));
                }

                sb.append(String.format("%s%s%s\n", batchAddQuotes(first, prefix) + key, ASSIGNMENT_MARK, value));
            }// fi (entry.getValue())

            first = true;

        }// for

        svalue = sb.toString();

        return svalue;
    }
    
    private String batchAddQuotes(boolean first, String prefix) {
        String result = null;
        if (!first || prefix == null || prefix.length() == 0) {
            result = EMPTY_PREFIX;
        } else {
            result = debugStr("Pfix:") + transformPrefix(prefix) + PROPERTY_SEPARATOR + debugStr(":");
        }
        return result;
    }

    /**
     * quotes every supart of prefix foo.bar.boo, so the result would be: foo."bar"."boo"
     * @param prefix
     */
    private String transformPrefix(String prefix) {
        String[] parts = prefix.split("\\" + PROPERTY_SEPARATOR);
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < parts.length; i++) {
            if (i == 0) {
                result.append(parts[i]);
            } else {
                result.append(String.format("%s\"%s\"", PROPERTY_SEPARATOR, parts[i]));
            }
        }
        return result.toString();
    }

    private String concatToPrefix(String prefix, Object key) {
        return prefix + ((prefix == null || prefix.length() == 0)? EMPTY_PREFIX :PROPERTY_SEPARATOR) + key;
    }

    /** debugging outputs for the parser*/
    private String debugStr(String string) {
        if (DEBUG_ON) {
            return string;
        }
        return "";
    }

    /** does the same as {@link GroovyDataProvider#flattenMap(Object, String)} for lists*/
    private String flattenList(Object obj) {
        String svalue = null;
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) obj;
        for (Object item : list) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(flatten(item));
            first = false;
        }

        svalue = String.format("[ %s ]", sb.toString());
        
        return svalue;
    }

    /**
     * flattens a Map
     * @param obj
     * @return the string representation
     */
    private String flattenMap(Object obj) {
        String svalue = null;
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        
        @SuppressWarnings("unchecked")
        Map<Object, Object> objMap = (Map<Object, Object>) obj;
        for (Map.Entry<Object, Object> entry : objMap.entrySet()) {
            String key = flatten(entry.getKey());
            String value = flatten(entry.getValue());
            if (!first) {
                sb.append(", ");
            }
            sb.append(String.format(" %s : %s ", key, value));
            first = false;
        }
        
        svalue = String.format("[ %s ]", sb.toString());
        
        return svalue;
    }

    public void dispose() {
        writeDataToFile();
        writeQueriedDumpToFile();
        cache.clear();
    }
}

/** helper enum, switch for quoting the strings in {@link GroovyDataProvider#flatten(Object)} method */
enum Chooser {
    QUOTED, 
    NOT_QUOTED
}
