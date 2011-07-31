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
package org.identityconnectors.contract.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.contract.data.DataProvider;
import org.identityconnectors.contract.data.GroovyDataProvider;
import org.identityconnectors.contract.exceptions.ContractException;
import org.identityconnectors.contract.exceptions.ObjectNotFoundException;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConfigurationProperty;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.api.RemoteFrameworkConnectionInfo;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.TestApiOp;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.identityconnectors.framework.common.objects.SyncDeltaType;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;


/**
 * Class holding various helper methods used by contract test suite
 * 
 * @author Dan Vernon
 * @author Tomas Knappek
 * @author Zdenek Louzensky
 */
public class ConnectorHelper {
    /**
     * Logging..
     */
    private static final Log LOG = Log.getLog(ConnectorHelper.class);
    
    private static final String JVM_ARG_DATA_PROVIDER = "data-provider";
    private static final Class<? extends DataProvider> DEFAULT_DATA_PROVIDER = GroovyDataProvider.class;

    public static DataProvider createDataProvider() {
        DataProvider dp = null;
        try {            
            String customDataProvider = System.getProperty(JVM_ARG_DATA_PROVIDER);
            if (customDataProvider != null) {
                Class<?> dpClass = null;
                dpClass = Class.forName(customDataProvider);
                if (!DataProvider.class.isAssignableFrom(dpClass)) {
                    /*
                     * The Class is not an instanceof DataProvider, so we cannot
                     * use it.
                     */
                    LOG.info("Class {0} is not assignable as DataProvider", customDataProvider);
                    throw new Exception("Class " + customDataProvider + " is not of type "
                            + DataProvider.class.getName());
                }
                dp = (DataProvider) dpClass.newInstance();
            } else {
                LOG.info("DataProvider class not specified, using default ''{0}''.", DEFAULT_DATA_PROVIDER);
                dp = (DataProvider) DEFAULT_DATA_PROVIDER.newInstance();
            }            
        } catch (Exception ex) {
            throw ContractException.wrap(ex);
        }
        
        return dp;

    }

    /**
     * Gets {@link ConfigurationProperties} for the connector
     * 
     * @param dataProvider
     * @return
     */
    public static ConfigurationProperties getConfigurationProperties(DataProvider dataProvider) {
        ConnectorInfoManager manager = getInfoManager(dataProvider);

        APIConfiguration apiConfig = getDefaultConfigurationProperties(
                dataProvider, manager);
        
        ConfigurationProperties properties = apiConfig.getConfigurationProperties();
        return properties;
    }
    
    /**
     * Creates connector facade, initializes connector configuration from dataProvider. propertyPrefix is added before
     * configuration properties. 
     */
    private static ConnectorFacade createConnectorFacade(DataProvider dataProvider, final String propertyPrefix) {
        ConnectorInfoManager manager = getInfoManager(dataProvider);
        Assert.assertNotNull("Manager can't be null, check configuration properties !", manager);

        APIConfiguration apiConfig = getDefaultConfigurationProperties(
                dataProvider, manager);
        
        ConfigurationProperties properties = apiConfig.getConfigurationProperties();
        
        List<String> propertyNames=properties.getPropertyNames();
        for(String propName : propertyNames) {
            ConfigurationProperty prop =  properties.getProperty(propName);
            LOG.info("OldValue = " + propName + " = \'" + 
                    prop.getValue() + "\' type = \'" + prop.getType() + "\'");

            try {
                final String tmpPropName;
                if (propertyPrefix != null) {
                    tmpPropName = propertyPrefix + "." + propName;
                }
                else tmpPropName = propName;
                
                Object configObject = dataProvider.getConnectorAttribute(
                        tmpPropName);
                
                if (configObject != null) {
                    LOG.info("Setting property ''{0}'' to value ''{1}''",
                            propName, configObject.toString());
                    properties.setPropertyValue(propName, configObject);
                } else {
                    LOG.warn(
                            "No value found for connector property ''{0}''",
                            propName);
                }
            } catch (ObjectNotFoundException ex) {
                LOG.info("Caught Object not found exception, propName: " + propName);
            }
        }
        
        LOG.info("----------------------------------");
        for(String propName : propertyNames) {
            ConfigurationProperty prop =  properties.getProperty(propName);
            LOG.info(propName + " = \'" + prop.getValue() + 
                    "\' type = \'" + prop.getType() + "\'");
        }

        ConnectorFacade connector = ConnectorFacadeFactory.getInstance().newInstance(apiConfig);
        Assert.assertNotNull("Unable to create connector", connector);

        return connector;
    }
    
    /**
     * Creates connector facade with wrong configuration.
     * @param wrongPropertyMap wrong configuration
     */
    public static ConnectorFacade createConnectorFacadeWithWrongConfiguration(DataProvider dataProvider, final Map<?,?> wrongPropertyMap) {
        ConnectorInfoManager manager = getInfoManager(dataProvider);
        Assert.assertNotNull("Manager can't be null, check configuration properties !", manager);

        APIConfiguration apiConfig = getDefaultConfigurationProperties(
                dataProvider, manager);
        
        ConfigurationProperties properties = apiConfig.getConfigurationProperties();
        
        List<String> propertyNames = properties.getPropertyNames();
        for (String propName : propertyNames) {
            ConfigurationProperty prop = properties.getProperty(propName);
            LOG.info("OldValue = " + propName + " = \'" + prop.getValue()
                    + "\' type = \'" + prop.getType() + "\'");

            final Object wrongProp = wrongPropertyMap.get(propName);

            // choose the wrong or default property value depending on what is
            // available.
            try {
                Object setProperty = (!wrongPropertyMap.containsKey(propName)) ? dataProvider
                        .getConnectorAttribute(propName) : wrongProp;

                LOG.info("Setting property ''{0}'' to value ''{1}''", propName,
                        ((setProperty == null)? "null" : setProperty.toString()));

                properties.setPropertyValue(propName, setProperty);

            } catch (ObjectNotFoundException ex) {
                // expected
            }
        }

        LOG.info("--------------- NEW PROPERTIES -------------------");
        for (String propName : propertyNames) {
            ConfigurationProperty prop = properties.getProperty(propName);
            LOG.info(propName + " = \'" + prop.getValue() + "\' type = \'"
                    + prop.getType() + "\'");
        }

        ConnectorFacade connector = ConnectorFacadeFactory.getInstance().newInstance(apiConfig);
        Assert.assertNotNull("Unable to create connector", connector);

        return connector;
    }
    
    /**
     * Creates connector facade, initializes connector configuration from
     * dataProvider and validates configuration and/or tests connection.
     */
    public static ConnectorFacade createConnectorFacade(DataProvider dataProvider) {
        ConnectorFacade connector = createConnectorFacade(dataProvider, null);
        
        // try to test connector configuration and established connection
        if (connector.getSupportedOperations().contains(TestApiOp.class)) {
            connector.test();
        } else {
            LOG.warn("Unable to test validity of connection.  Connector does not suport the Test API. " +
            		"Trying at least to test validity of configuration.");
            connector.validate();
        }
        
        return connector;
    }
    
    /**
     * Performs search on connector facade and filters only searched object by its name.
     * @return found object
     */
    static ConnectorObject findObjectByName(ConnectorFacade connectorFacade, 
            ObjectClass objClass, String name, OperationOptions opOptions) {        
        Filter nameFilter = FilterBuilder.equalTo(new Name(name));        
        final List<ConnectorObject> foundObjects = new ArrayList<ConnectorObject>();
        connectorFacade.search(objClass, nameFilter,
                new ResultsHandler() {
                    public boolean handle(ConnectorObject obj) {
                        foundObjects.add(obj);
                        return false;
                    }
                }, opOptions);
        if(foundObjects.size() > 0) {
            Assert.assertEquals("Name should be unique, but found multiple objects with the same name",
                    1, foundObjects.size());
        } else {
            return null;
        }
        
        return foundObjects.get(0);
    }
    
    /**
     * Performs search on connector facade with specified object class, filter and operation options.
     * @return list of found objects.
     */
    public static List<ConnectorObject> search(ConnectorFacade connectorFacade, ObjectClass objClass, Filter filter, OperationOptions opOptions) {
        final List<ConnectorObject> foundObjects = new ArrayList<ConnectorObject>();
        connectorFacade.search(objClass, filter,
                new ResultsHandler() {
                    public boolean handle(ConnectorObject obj) {
                        foundObjects.add(obj);
                        return true;
                    }
                }, opOptions);        
        return foundObjects;
    }
    
    /**
     * Performs search on connector facade with specified object class, filter and operation options.
     * @return Map of {@link Uid}s to {@link ConnectorObject}s that were found. 
     */
    public static Map<Uid, ConnectorObject> search2Map(ConnectorFacade connectorFacade, ObjectClass objClass, Filter filter, OperationOptions opOptions) {
        final Map<Uid, ConnectorObject> foundObjects = new Hashtable<Uid, ConnectorObject>();
        connectorFacade.search(objClass, filter,
                new ResultsHandler() {
                    public boolean handle(ConnectorObject obj) {
                        foundObjects.put(obj.getUid(), obj);
                        return true;
                    }
                }, opOptions);        
        return foundObjects;
    }
    
    /**
     * Performs sync on connector facade.
     * @returns list of deltas
     */
    public static List<SyncDelta> sync(ConnectorFacade connectorFacade, ObjectClass objClass,
            SyncToken token, OperationOptions opOptions) {
        final List<SyncDelta> returnedDeltas = new ArrayList<SyncDelta>();

        connectorFacade.sync(objClass, token, new SyncResultsHandler() {
            public boolean handle(SyncDelta delta) {
                returnedDeltas.add(delta);
                return true;
            }
        }, opOptions);

        return returnedDeltas;
    }
    
    /**
     * Performs deletion of object specified by uid.
     * Fails in case failOnError is true and object wasn't deleted during delete call.
     */
    public static boolean deleteObject(ConnectorFacade connectorFacade, 
            ObjectClass objClass, Uid uid, boolean failOnError, OperationOptions opOptions) {
        boolean deleted = false;
        
        Assert.assertFalse("Connector helper deleteObject method received a null Uid, when it was told it should not fail",
                (failOnError && (uid == null)) );
        
        if(uid == null) {
            return deleted;
        }
        
        try {
            connectorFacade.delete(objClass, uid, opOptions);
        } catch (Throwable t) {
            if(failOnError) {
                Assert.fail("Connector helper deleteObject method caught an exception, when it was told it should not fail");
            } 
        }
        
        if(failOnError) {
            // at present javadoc for delete is not clear about what
            // happens if delete fails, so I'll verify it's gone by searching
            ConnectorObject obj = connectorFacade.getObject(objClass, uid, opOptions);            
            Assert.assertNull("The deleted object was found. It should be no longer on the resource.", obj);
            deleted = true;
        }
        
        return deleted;
    }
    
        
    /**
     * Checks if object has expected attributes and values. All readable or non-special attributes are checked.
     */
    public static boolean checkObject(ObjectClassInfo objectClassInfo, ConnectorObject connectorObj,
            Set<Attribute> requestedAttributes) {
        return checkObject(objectClassInfo, connectorObj, requestedAttributes, true);
    }
    
    /**
     * Checks if object has expected attributes and values. All readable or non-special attributes are checked.
     * @param checkNotReturnedByDefault if true then also attributes not returned by default are checked 
     */
    public static boolean checkObject(ObjectClassInfo objectClassInfo, ConnectorObject connectorObj,
            Set<Attribute> requestedAttributes, boolean checkNotReturnedByDefault) {
        boolean success = true;

        for (Attribute attribute : requestedAttributes) {
            // we will check all attributes that are readable all other attributes
            // should not be present in connector object
            if (isReadable(objectClassInfo, attribute)) {
                if (checkNotReturnedByDefault || isReturnedByDefault(objectClassInfo, attribute)) {
                    Attribute createdAttribute = connectorObj.getAttributeByName(attribute.getName());
                    if (createdAttribute == null)
                        Assert.fail(String.format("Attribute '%s' is missing.", attribute.getName()));
                        
                    List<Object> fetchedValue = createdAttribute.getValue();
                    List<Object> requestedValue = attribute.getValue();
                    String msg = String.format("Attribute '%s' was not properly created. Requested values: %s Fetched values: %s", attribute.getName(), requestedValue, fetchedValue);
                    Assert.assertTrue(msg, checkValue(fetchedValue, requestedValue));
                }
            }
        }

        return success;
    }
    
    static <E> boolean checkValue(List<E> fetchedValues, List<E> expectedValues) {
        Iterator<E> e = expectedValues.iterator();
        List<E> fetchedValuesClone = CollectionUtil.newList(fetchedValues);
        while (e.hasNext()) {
            E expected = e.next();

            boolean found = false;
            Iterator<E> f = fetchedValuesClone.iterator();
            while (f.hasNext()) {
                if (CollectionUtil.equals(expected, f.next())) {
                    f.remove();
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check that passed SyncDelta has exptected values.
     */
    public static void checkSyncDelta(ObjectClassInfo ocInfo, SyncDelta delta, Uid uid, Set<Attribute> attributes, SyncDeltaType deltaType, boolean checkNotReturnedByDefault) {
        // check that Uid is correct
        String msg = "Sync returned wrong Uid, expected: %s, returned: %s.";
        assertEquals(String.format(msg, uid, delta.getUid()), delta.getUid(), uid);

        if (deltaType != SyncDeltaType.DELETE) {
            // check that attributes are correct
            ConnectorHelper.checkObject(ocInfo, delta.getObject(), attributes, checkNotReturnedByDefault);
        }

        // check that delta type is expected
        msg = "Sync delta type should be %s, but returned: %s.";
        assertTrue(String.format(msg, deltaType, delta.getDeltaType()), delta.getDeltaType() == deltaType);
    }         
    
    /**
     * Whether is attribute readable.
     */
    public static boolean isReadable(ObjectClassInfo objectClassInfo, Attribute attribute) {
        boolean isReadable = false;
        Set<AttributeInfo> attributeInfoSet = objectClassInfo.getAttributeInfo();
        for(AttributeInfo attributeInfo : attributeInfoSet) {
            if(attributeInfo.is(attribute.getName())) {
                isReadable = attributeInfo.isReadable();
                break;
            }
        }
        return isReadable;
    }
    
    /**
     * Whether is attribute required.
     */
    public static boolean isRequired(ObjectClassInfo objectClassInfo, Attribute attribute) {
        boolean isRequired = false;
        Set<AttributeInfo> attributeInfoSet = objectClassInfo.getAttributeInfo();
        for(AttributeInfo attributeInfo : attributeInfoSet) {
            if(attributeInfo.is(attribute.getName())) {
                isRequired = attributeInfo.isRequired();
                break;
            }
        }
        return isRequired;
    }
    
    /**
     * Whether is attribute Createable.
     */
    public static boolean isCreateable(ObjectClassInfo objectClassInfo, Attribute attribute) {
        boolean isCreateable = false;
        Set<AttributeInfo> attributeInfoSet = objectClassInfo.getAttributeInfo();
        for(AttributeInfo attributeInfo : attributeInfoSet) {
            if(attributeInfo.is(attribute.getName())) {
                isCreateable = attributeInfo.isCreateable();
                break;
            }
        }
        return isCreateable;
    }
    
    /**
     * Whether is attribute readable.
     */
    public static boolean isUpdateable(ObjectClassInfo objectClassInfo, Attribute attribute) {
        boolean isUpdateable = false;
        Set<AttributeInfo> attributeInfoSet = objectClassInfo.getAttributeInfo();
        for(AttributeInfo attributeInfo : attributeInfoSet) {
            if(attributeInfo.is(attribute.getName())) {
                isUpdateable = attributeInfo.isUpdateable();
                break;
            }
        }
        return isUpdateable;
    }
    
    /**
     * Whether is attribute returnedByDefault.
     */
    public static boolean isReturnedByDefault(ObjectClassInfo objectClassInfo, Attribute attribute) {
        boolean isReturnedByDefault = false;
        Set<AttributeInfo> attributeInfoSet = objectClassInfo.getAttributeInfo();
        for(AttributeInfo attributeInfo : attributeInfoSet) {
            if(attributeInfo.is(attribute.getName())) {
                isReturnedByDefault = attributeInfo.isReturnedByDefault();
                break;
            }
        }
        return isReturnedByDefault;
    }
    
    /**
     * Whether is attribute multiValue.
     */
    public static boolean isMultiValue(ObjectClassInfo objectClassInfo, String attribute) {
        boolean isMultiValue = false;
        Set<AttributeInfo> attributeInfoSet = objectClassInfo.getAttributeInfo();
        for(AttributeInfo attributeInfo : attributeInfoSet) {
            if(attributeInfo.is(attribute)) {
                isMultiValue = attributeInfo.isMultiValued();
                break;
            }
        }
        return isMultiValue;
    }
    
    /**
     * Whether is attribute creatable, updateable and readable.
     */
    public static boolean isCRU(ObjectClassInfo oinfo, String attribute) {
        boolean cru = false;
        for (AttributeInfo ainfo : oinfo.getAttributeInfo()) {
            if (ainfo.is(attribute)) {
                if (ainfo.isCreateable() && ainfo.isUpdateable() && ainfo.isReadable()) {
                    cru = true;
                }
                break;
            }
        }
        return cru;
    }
    
    /**
     * Whether is attribute readable.
     */
    public static boolean isReadable(ObjectClassInfo oinfo, String attribute) {
        for (AttributeInfo ainfo : oinfo.getAttributeInfo()) {
            if (ainfo.is(attribute)) {
                return ainfo.isReadable();
            }
        }
        
        return false;        
    }
    
    /**
     * Whether is attribute supported.
     */
    public static boolean isAttrSupported(ObjectClassInfo oinfo, String attribute) {
        for (AttributeInfo ainfo : oinfo.getAttributeInfo()) {
            if (ainfo.is(attribute)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get updateable attributes' values.
     * 
     * Generate new values for updateable attributes based on contract test
     * properties prefixed by <code>qualifier</code>
     * 
     * @param qualifier
     *            the prefix for values used in update.
     */ 
    public static Set<Attribute> getUpdateableAttributes(DataProvider dataProvider, 
            ObjectClassInfo objectClassInfo, 
            String testName, String qualifier, int sequenceNumber, boolean checkRequired, boolean onlyMultiValue) {
        return getAttributes(dataProvider, objectClassInfo, testName, qualifier, sequenceNumber, checkRequired, onlyMultiValue, false, true);
    }
    
    /**
     * Get createable attributes' values.
     */
    public static Set<Attribute> getCreateableAttributes(DataProvider dataProvider, 
            ObjectClassInfo objectClassInfo, 
            String testName, int sequenceNumber, boolean checkRequired, boolean onlyMultiValue) {
        return getAttributes(dataProvider, objectClassInfo, testName, "", sequenceNumber, checkRequired, onlyMultiValue, true, false);
    }
    
    /**
     * Returns set of attributes' names which are readable.
     */
    public static Set<String> getReadableAttributesNames(ObjectClassInfo ocInfo) {
        Set<String> readableAttrs = new HashSet<String>();
        for (AttributeInfo ainfo : ocInfo.getAttributeInfo()) {
            if (ainfo.isReadable()) {
                readableAttrs.add(ainfo.getName());
            }
        }
        
        return readableAttrs;
    }
    
    /**
     * get attribute values (concatenates the qualifier with the name)
     * @param dataProvider
     * @param objectClassInfo
     * @param testName
     * @param qualifier
     * @param sequenceNumber
     * @param checkRequired
     * @return
     * @throws org.identityconnectors.contract.exceptions.ObjectNotFoundException
     */
    public static Set<Attribute> getAttributes(DataProvider dataProvider, 
            ObjectClassInfo objectClassInfo, String testName, 
            String qualifier, int sequenceNumber, 
            boolean checkRequired, boolean onlyMultiValue, boolean onlyCreateable, boolean onlyUpdateable) throws ObjectNotFoundException {
        Set<Attribute> attributes = new HashSet<Attribute>();        
        
        
        for(AttributeInfo attributeInfo : objectClassInfo.getAttributeInfo()) {
            if (onlyMultiValue && !attributeInfo.isMultiValued()) {
                continue;
            }
            if (onlyCreateable && !attributeInfo.isCreateable()) {
                continue;
            }
            if (onlyUpdateable && !attributeInfo.isUpdateable()) {
                continue;
            }
            String attributeName = attributeInfo.getName();
            try {
                // if the attribute is not UID, get a value from the dataprovider
                // and add an attribute (exception is thrown if value is not present
                // values for UID cannot be generated because some connectors have mapping of
                // UID and NAME to same values - check test would fail
                if(!attributeInfo.is(Uid.NAME)) {                    
                    String dataName = attributeName;
                    if (qualifier.length() > 0) {
                        dataName = qualifier + "." + dataName;
                    }

                    // *multivalue* attributes have different default values. That is why we should
                    // pass this to get().
                    Object attributeValue = get(dataProvider, testName, attributeInfo.getType()
                            , dataName, objectClassInfo.getType(), sequenceNumber, attributeInfo.isMultiValued());
                    
                    if(attributeValue instanceof Collection<?>) {
                        attributes.add(AttributeBuilder.build(attributeName, (Collection<?>)attributeValue));
                    } else {
                        attributes.add(AttributeBuilder.build(attributeName, attributeValue));
                    }
                }
            } catch (ObjectNotFoundException ex) {
                // caught an exception because no value was supplied for an attribute
                if(checkRequired && attributeInfo.isRequired()) {
                    // if the attribute was required, it's an error
                    LOG.error(ex, "Could not find a value of REQUIRED attribute type ''{0}'' for ''{1}''", 
                            attributeInfo.getType(), attributeName);
                    throw ex;
                } else {
                    // if the attribute was not required, it's a warning
                    LOG.warn("Could not find a value of type ''{0}'' for ''{1}''", 
                            attributeInfo.getType(), attributeName);
                }
            }
        }
        
        return attributes;
    }
        
    /**
     * gets the attributes for you, appending the qualifier to the attribute name
     * @param connectorFacade
     * @param dataProvider
     * @param objectClassInfo
     * @param testName
     * @param qualifier
     * @param sequenceNumber
     * @return
     * @throws org.identityconnectors.contract.exceptions.ObjectNotFoundException
     */
    public static Uid createObject(ConnectorFacade connectorFacade, 
            DataProvider dataProvider, ObjectClassInfo objectClassInfo,
            String testName, String qualifier, 
            int sequenceNumber, OperationOptions opOptions) throws ObjectNotFoundException {
        Set<Attribute> attributes = getAttributes(dataProvider, objectClassInfo, testName,
                qualifier, sequenceNumber, true, false, true, false);
        
        return connectorFacade.create(getObjectClassFromObjectClassInfo(objectClassInfo), attributes, opOptions);        
    }
        
    /**
     * gets the attributes for you
     * @param connectorFacade
     * @param dataProvider
     * @param objectClassInfo
     * @param testName
     * @param sequenceNumber
     * @return
     * @throws org.identityconnectors.contract.exceptions.ObjectNotFoundException
     */
    public static Uid createObject(ConnectorFacade connectorFacade, 
            DataProvider dataProvider, ObjectClassInfo objectClassInfo,
            String testName, int sequenceNumber, OperationOptions opOptions) throws ObjectNotFoundException {
        Set<Attribute> attributes = getCreateableAttributes(dataProvider, objectClassInfo,
                testName, sequenceNumber, true, false);

        return connectorFacade.create(getObjectClassFromObjectClassInfo(objectClassInfo), attributes, opOptions);
    }
    
    /**
     * check to see if a particular objectclass supports a particular operation
     * 
     * @param connectorFacade
     * @param typeQuery
     * @param operation
     * @return
     */
    public static boolean operationSupported(ConnectorFacade connectorFacade,
            ObjectClass oClass, Class<? extends APIOperation> operation) {
        
        Set<Class<? extends APIOperation>> s = new HashSet<Class<? extends APIOperation>>();
        s.add(operation);

        return operationsSupported(connectorFacade, oClass, s);
    }
    
    /**
     * check to see if a particular objectclass supports a particular operations
     * 
     * @param connectorFacade
     * @param typeQuery
     * @param operation1
     * @param operation2
     * @return
     */
    public static boolean operationSupported(ConnectorFacade connectorFacade,
            ObjectClass oClass, Class<? extends APIOperation> operation1,
            Class<? extends APIOperation> operation2) {

        Set<Class<? extends APIOperation>> s = new HashSet<Class<? extends APIOperation>>();
        s.add(operation1);
        s.add(operation2);

        return operationsSupported(connectorFacade, oClass, s);
    }
    
    /**
     * check to see if a particular objectclass supports a particular operations
     * To succeed all the operations must be supported.
     * 
     * @param connectorFacade
     * @param typeQuery
     * @param operation
     * @return
     */
    public static boolean operationsSupported(ConnectorFacade connectorFacade, ObjectClass oClass, 
            Set<Class<? extends APIOperation>> operations) {
        List<Boolean> opsSupported = new ArrayList<Boolean>();
        
        // get the schema
        Schema schema = connectorFacade.schema();
        Assert.assertNotNull("Connector did not return a schema", schema);
        for (Class<? extends APIOperation> op : operations) {
            Set<ObjectClassInfo> ocInfoSet = schema.getSupportedObjectClassesByOperation(op);

            // for each ObjectClassInfo in the schema ...
            boolean currentOpSupported = false;
            for (ObjectClassInfo ocInfo : ocInfoSet) {
                // get the type of the ObjectClassInfo
                if (ConnectorHelper.getObjectClassFromObjectClassInfo(ocInfo).equals(oClass)) {
                    currentOpSupported = true;
                    break;
                } 
            }//for each object class
            
            opsSupported.add(currentOpSupported);
        }//for each operation
        
        // do and throughout results of every operation
        // to verify if all are supported
        boolean result = true;
        for (Boolean bool : opsSupported) {
            result = result & bool;
            if (result == false) {
                break;
            }
        }
        
        return result;
    }
        
    /**
     * check to see if ANY objectclass supports a particular operation
     * @param connectorFacade
     * @param operation
     * @return
     */
    public static boolean operationSupported(ConnectorFacade connectorFacade,
            Class<? extends APIOperation> operation) {
        Set<Class<? extends APIOperation>> s = new HashSet<Class<? extends APIOperation>>();
        s.add(operation);
        
        return operationsSupported(connectorFacade, s);
    }
    
    /**
     * check to see if ANY objectclass supports a particular operation
     * @param connectorFacade
     * @param operation1
     * @param operation2
     * @return
     */
    public static boolean operationSupported(ConnectorFacade connectorFacade,
            Class<? extends APIOperation> operations1, Class<? extends APIOperation> operations2) {
        Set<Class<? extends APIOperation>> s = new HashSet<Class<? extends APIOperation>>();
        s.add(operations1);
        s.add(operations2);
        
        return operationsSupported(connectorFacade, s);
    }
    
    /**
     * check to see if ANY objectclass supports a particular operations
     * @param connectorFacade
     * @param operation
     * @return
     */
    public static boolean operationsSupported(ConnectorFacade connectorFacade,
            Set<Class<? extends APIOperation>> operations) {
        boolean opSupported = false;
        
        Schema schema = connectorFacade.schema();
        Assert.assertNotNull("Connector did not return a schema", schema);
        Set<ObjectClassInfo> objectClassInfoSet = schema.getObjectClassInfo();
        
        for(ObjectClassInfo objectClassInfo : objectClassInfoSet) {
            if(operationsSupported(connectorFacade, ConnectorHelper.getObjectClassFromObjectClassInfo(objectClassInfo), operations)) {
                opSupported = true;
                break;
            }
        }
        
        return opSupported;
    }

    /**
     * Tries to create remote or local manager.
     * Remote manager is created in case all connectorserver properties are set. If connectorserver properties are missing
     * or remote manager creation fails then tries to create local manager.
     */
    public static ConnectorInfoManager getInfoManager(final DataProvider dataProvider) {
        ConnectorInfoManagerFactory fact = ConnectorInfoManagerFactory.getInstance();
        ConnectorInfoManager manager = null;
        
        String useConnectorServer = System.getProperty("useConnectorServer"); 
        if ("true".equals(useConnectorServer)) {
            LOG.info("TESTING CONNECTOR ON CONNECTOR SERVER.");
            manager = getRemoteManager(dataProvider, fact);
        }
        else {
            LOG.info("TESTING LOCAL CONNECTOR.");
            manager = getLocalManager(dataProvider, fact);
        }
        
        assertNotNull("Manager wasn't created - check *MANDATORY* properties.", manager);
        
        return manager;
    }

    /**
     * Returns local manager or null.
     * @param dataProvider
     * @param fact
     * @return null in case configuration is NOT provided
     * @throws RuntimeException if creation fails although properties were provided
     */
    private static ConnectorInfoManager getLocalManager(final DataProvider dataProvider,
            final ConnectorInfoManagerFactory fact) {      
        ConnectorInfoManager manager = null;
        
        // try to load bundleJar property (which should be set by ant)
        File bundleJar = new File(((String) dataProvider.getTestSuiteAttribute("bundleJar")).trim());
        Assert.assertTrue("BundleJar does not exist: " + bundleJar.getAbsolutePath(), bundleJar
                .isFile());
        try {
            manager = fact.getLocalManager(bundleJar.toURI().toURL());
        } catch (MalformedURLException ex) {
            throw ContractException.wrap(ex);
        }

        return manager;
    }

    /**
     * Returns remote manager or null.
     * 
     * @param dataProvider
     * @param fact
     * @return null in case configuration is NOT provided
     * @throws RuntimeException
     *             in case creation fails although configuration properties were
     *             provided
     */
    private static ConnectorInfoManager getRemoteManager(final DataProvider dataProvider,
            final ConnectorInfoManagerFactory fact) {        
        ConnectorInfoManager manager = null;

        String host = null;
        Integer port = null;
        String key = null;
        // load properties from config file and then override them with system properties
        try {
            host = (String)dataProvider.getTestSuiteAttribute("serverHost");
        }
        catch (ObjectNotFoundException ex) {  //ok
        }
        try {
            port = (Integer)dataProvider.getTestSuiteAttribute("serverPort");
        }
        catch (ObjectNotFoundException ex) {  //ok
        }
        try {
            key = (String)dataProvider.getTestSuiteAttribute("serverKey");
        }
        catch (ObjectNotFoundException ex) {  //ok
        }        
        // now override with system properties, if set
        if (StringUtil.isNotBlank(System.getProperty("serverHost"))) {               
            host = System.getProperty("serverHost");
        }
        if (StringUtil.isNotBlank(System.getProperty("serverPort"))) {               
            port = Integer.parseInt(System.getProperty("serverPort"));
        }
        if (StringUtil.isNotBlank(System.getProperty("serverKey"))) {               
            key = System.getProperty("serverKey");
        }
                
        Assert.assertTrue("Connector server host not set.", StringUtil.isNotBlank(host));
        Assert.assertNotNull("Connector server port not set.", port);
        Assert.assertTrue("Connector server key not set.", StringUtil.isNotBlank(key));

        try {
            // try to connect to remote manager
            manager = fact.getRemoteManager(new RemoteFrameworkConnectionInfo(host, port,
                new GuardedString(key.toCharArray())));
        }
        catch (Throwable t) {
            // wrap in exception rather to fail to have full stacktrace
            throw new ContractException("Cannot create remote manager. Check connector server settings.", t);
        }

        return manager;
    }
    
    public static APIConfiguration getDefaultConfigurationProperties(DataProvider dataProvider,
            ConnectorInfoManager manager) throws ObjectNotFoundException {
        
        String bundleName = (String) dataProvider.getTestSuiteAttribute("bundleName");
        String bundleVersion = (String) dataProvider.getTestSuiteAttribute("bundleVersion");
        String connectorName = (String) dataProvider.getTestSuiteAttribute("connectorName");
        ConnectorKey key = new ConnectorKey(bundleName, bundleVersion, connectorName);
        ConnectorInfo info = manager.findConnectorInfo(key);
        final String MSG = "Connector info wasn't found. Check values of bundleName, bundleVersion and connectorName properties." + 
                            "\nbundleName:%s\nbundleVersion:%s\nconnectorName:%s";                            
        Assert.assertNotNull(String.format(MSG, bundleName, bundleVersion, connectorName), info);
        APIConfiguration apiConfig = info.createDefaultAPIConfiguration();

        return apiConfig;
    }
    
    private static String formatDataName(String objectClassName, String name) {
        StringBuilder sbPath = new StringBuilder(objectClassName);
        sbPath.append(".");
        sbPath.append(name);
        return sbPath.toString();
   }
    
    /**
     * no sequence number or qualifier, appends objectclass to name
     * @param dataProvider
     * @param componentName
     * @param name
     * @param objectClassName
     * @return
     * @throws org.identityconnectors.contract.exceptions.ObjectNotFoundException
     */
    public static String getString(DataProvider dataProvider, String componentName, 
            String name, String objectClassName) throws ObjectNotFoundException {
        return dataProvider.getString(formatDataName(objectClassName, name), 
                componentName);
    }
    
    public static Object get(DataProvider dataProvider, String componentName, 
            Class<?> dataTypeName, String name, String objectClassName, 
            int sequenceNumber, boolean isMultivalue) throws ObjectNotFoundException {
        return dataProvider.get(dataTypeName, formatDataName(objectClassName, name), 
                componentName, sequenceNumber, isMultivalue);
    }
    
    public static Object get(DataProvider dataProvider, String componentName, 
            Class<?> dataTypeName, String name, String qualifier, String objectClassName, 
            int sequenceNumber, boolean isMultivalue) throws ObjectNotFoundException {
        return dataProvider.get(dataTypeName, formatDataName(objectClassName, formatDataName(qualifier, name)), 
                componentName, sequenceNumber, isMultivalue);
    }


    /**
     * Returns object class based on object class info.
     */
    public static ObjectClass getObjectClassFromObjectClassInfo(final ObjectClassInfo objectClassInfo) {
        return new ObjectClass(objectClassInfo.getType());
    }
}
