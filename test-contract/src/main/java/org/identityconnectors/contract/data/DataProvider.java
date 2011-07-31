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

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.spi.Configuration;

/**
 * DataProvider is a facility used for getting (reading/generating) data for
 * Contract test suite.
 * 
 * @author Dan Vernon
 */
public interface DataProvider {        

    /**
     * Gets data value by the specified parameters
     * 
     * @param dataTypeName
     * @param name
     * @param componentName
     * @param sequenceNumber
     * @param isMultivalue switch between single and multivalue query
     * @return 
     * @throws org.identityconnectors.contract.data.DataProvider.ObjectNotFoundException
     */
    public Object get(Class<?> dataTypeName, String name,
            String componentName, int sequenceNumber, boolean isMultivalue);
    
    /**
     * Gets data value by the specified parameters
     * 
     * @param dataTypeName
     * @param name
     * @param componentName
     * @return
     * @throws org.identityconnectors.contract.data.DataProvider.ObjectNotFoundException
     */
    public Object get(Class<?> dataTypeName, String name,
            String componentName);

    /**
     * Gets data value by the specified parameters
     * 
     * @param name
     * @param componentName
     * @param sequenceNumber
     * @return
     * @throws org.identityconnectors.contract.data.DataProvider.ObjectNotFoundException
     */
    public String getString(String name,
            String componentName, int sequenceNumber);
    
    /**
     * Gets data value by the specified parameters
     * 
     * @param name
     * @param componentName
     * @return
     * @throws org.identityconnectors.contract.data.DataProvider.ObjectNotFoundException
     */
    public String getString(String name,
            String componentName);
    
    /**
     * Gets data value by the specified parameters
     * @param propName
     * 
     * @return
     * @throws org.identityconnectors.contract.data.DataProvider.ObjectNotFoundException
     */
    public Object getConnectorAttribute(String propName);
    
    /**
     * Gets test suite attribute
     * @param propName
     * 
     * @return
     * @throws org.identityconnectors.contract.data.DataProvider.ObjectNotFoundException
     */
    public Object getTestSuiteAttribute(String propName);
    
    /**
     * Gets test suite attribute
     * @param propName
     * 
     * @return
     * @throws org.identityconnectors.contract.data.DataProvider.ObjectNotFoundException
     */
    public Object getTestSuiteAttribute(String propName, 
            String testName);
    
    /* *********** METHODS FOR UNIT TESTS ************** */
    
    /**
     * Acquire a property value for given name
     */
    public Object get(String name);
    
    /**
     * Aquire a property value marked with given iteration,
     * for example i1.testProperty
     * 
     * @param name the suffix
     * @param sequenceNumber
     * @return the property value
     */
    public Object get(String name, int sequenceNumber);
    
    /**
     * <p>
     * Random generator uses a <strong>pattern</strong> to generate a random
     * sequence based on given pattern.
     * </p>
     * <p>
     * the supported characters are (can appear in pattern string):
     * </p>
     * <ul>
     * <li># - numeric</li>
     * <li>a - lowercase letter</li>
     * <li>A - uppercase letter</li>
     * <li>? - lowercase and uppercase letter</li>
     * <li>. - any character</li>
     * </ul>
     * <p>
     * Any other character inside the pattern is directly printed to the output.
     * </p>
     * <p>
     * Backslash is used to escape any character. For instance pattern
     * "###\\.##" prints a floating point random number
     * </p>
     * 
     * @param pattern the pattern for generation
     * @param clazz the type of returned random object
     * @return randomly generated object with content based on given type. 
     */
    public Object generate(String pattern, Class<?> clazz);
    
    /**
     * generates a random string dynamically.
     * {@link DataProvider#generate(String, Class)}
     */
    public Object generate(String pattern);
    
    /* ***************** ADDITIONAL PROPERTY UTILS ************** */
    /**
     * adds to 'cfg' the complete map defined by property 'propertyName'
     * 
     * @param propertyName
     *            the name of property which represents the submap that will be
     *            converted to configuration
     * @param cfg
     *            the configuration that will be updated by information from
     *            property 'propertyName'
     *            <p>
     *            Sample usage:<br>
     * 
     * <pre>
     *     static final String DEFAULT_CONFIGURATINON = "configuration.init"
     *     
     *     // attempt to create the database in the directory..
     *     config = new ConnectorConfiguration();   
     *     
     *     // LOAD THE submap in 'configuration' <strong>prefix</prefix> to 'config' object.
     *     dataProvider.loadConfiguration(DEFAULT_CONFIGURATINON, config); 
     * //////// The groovy configuration 
     * 
     *     // account configurations   
     *     configuration{
     *       init.driver="foo"    
     *       init.hostName="bar"
     *       init.port="boo"
     *     }
     * </pre>
     * @throws NoSuchMethodException the Setter method for the property in the configuration does not exist
     * @throws IllegalAccessException 
     * @throws InvocationTargetException   
     * @throws SecurityException 
     */
    public void loadConfiguration(final String propertyName, Configuration cfg);
    
    /**
     * converts the given property submap to Attribute set.
     * 
     * @param propertySetName the property that marks the submap for conversion.
     * @return the converted attribute set
     * <p>
     * Sample usage:
     * <pre>
     *  createAttrs = dataProvider.getAttributeSet("account.create");
     *  
     *  //////// The groovy configuration 
     * 
     *     // account sets   
     *     account{
     *       create.driver="foo"    
     *       create.hostName="bar"
     *       create.port="boo"
     *       
     *       update.driver="foo2"    
     *       update.hostName="bar2"
     *       update.port="boo2"
     *     } 
     * </pre>
     */
    public Set<Attribute> getAttributeSet(final String propertySetName);
    
    /* ************************************************* */
    
    /** free the allocated resources */
    public void dispose();
    
}
