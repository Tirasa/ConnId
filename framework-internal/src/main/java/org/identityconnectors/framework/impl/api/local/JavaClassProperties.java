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
package org.identityconnectors.framework.impl.api.local;

import static org.identityconnectors.framework.common.FrameworkUtil.isSupportedConfigurationType;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.common.FrameworkUtil;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.serializer.SerializerUtil;
import org.identityconnectors.framework.impl.api.ConfigurationPropertiesImpl;
import org.identityconnectors.framework.impl.api.ConfigurationPropertyImpl;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.framework.spi.operations.SPIOperation;


/**
 * Class for translating from a Java class to ConfigurationProperties
 * and from ConfigurationProperties to a java class.
 */
public class JavaClassProperties 
{    
    /**
     * Given a configuration class, creates the configuration properties
     * for it.
     */
    public static ConfigurationPropertiesImpl
    createConfigurationProperties(Configuration config) {
        try {
            return createConfigurationProperties2(config);
        }
        catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }
    
    /**
     * Given a configuration class and populated properties,
     * creates a bean for it.
     */
    public static Configuration 
    createBean(ConfigurationPropertiesImpl properties,
    Class<? extends Configuration> configClass) {
        try {
            return createBean2(properties,configClass);
        }
        catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }
    
    /**
     * Given a configuration bean and populated properties,
     * merges the properties into the bean.
     */
    public static void
    mergeIntoBean(ConfigurationPropertiesImpl properties,
    Configuration config) {
        try {
            mergeIntoBean2(properties,config);
        }
        catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    private static ConfigurationPropertiesImpl 
    createConfigurationProperties2(Configuration defaultObject) 
    throws Exception {
        Class<? extends Configuration> config = defaultObject.getClass();
        ConfigurationPropertiesImpl properties = 
            new ConfigurationPropertiesImpl();
        List<ConfigurationPropertyImpl> temp = 
            new ArrayList<ConfigurationPropertyImpl>();
        Map<String,PropertyDescriptor> descs = getFilteredProperties(config);
        
        for (PropertyDescriptor desc : descs.values()) {
            Method getter = desc.getReadMethod();
            Method setter = desc.getWriteMethod();
            
            String name = desc.getName();
            
            // get the configuration options..
            ConfigurationProperty options = 
                getPropertyOptions(getter, setter);
            // use the options to set internal properties..
            int order = 0;
            String helpKey = name + ".help";
            String displKey = name + ".display";
            boolean confidential = false;
            boolean required = false;
            if (options != null) {
                // determine the display and help keys..
                if (StringUtil.isNotBlank(options.helpMessageKey())) {
                    helpKey = options.helpMessageKey();
                }
                if (StringUtil.isNotBlank(options.displayMessageKey())) {
                    displKey = options.displayMessageKey();
                }
                // determine the order..
                order = options.order();
                
                confidential = options.confidential();
                required = options.required();
            }
            Class<?> type;
            if ( desc instanceof IndexedPropertyDescriptor ) {
                type = Array.newInstance(desc.getPropertyType(), 0).getClass();
            }
            else {
                type = desc.getPropertyType();
            }
            if (!isSupportedConfigurationType(type)) { 
                final String MSG = "Property type ''{0}'' is not supported."; 
                throw new IllegalArgumentException(MessageFormat.format(MSG, type)); 
            } 

            Object value = getter.invoke(defaultObject);
            
            ConfigurationPropertyImpl prop = new ConfigurationPropertyImpl();
            prop.setConfidential(confidential);
            prop.setDisplayMessageKey(displKey);
            prop.setHelpMessageKey(helpKey);
            prop.setName(name);
            prop.setOrder(order);
            prop.setValue(value);
            prop.setType(type);
            prop.setRequired(required);
            prop.setOperations(options == null ? null : translateOperations(options.operations()));
            
            temp.add(prop);

        }
        properties.setProperties(temp);
        return properties;
    }
    
    private static Set<Class<? extends APIOperation>> translateOperations(Class<? extends SPIOperation> [] ops)
    {
        Set<Class<? extends APIOperation>> set =
            new HashSet<Class<? extends APIOperation>>();
        for (Class<? extends SPIOperation> spi : ops) {
            set.addAll(FrameworkUtil.spi2apis(spi));
        }
        return set;
    }

    private static Configuration createBean2(
            ConfigurationPropertiesImpl properties,
            Class<? extends Configuration> configClass) throws Exception {
        Configuration rv = configClass.newInstance();
        rv.setConnectorMessages(properties.getParent().getConnectorInfo().getMessages());
        mergeIntoBean2(properties,rv);
        return rv;
    }
    
    private static void mergeIntoBean2(
            ConfigurationPropertiesImpl properties,
            Configuration config) throws Exception {
        Class<? extends Configuration> configClass = config.getClass();
        Map<String, PropertyDescriptor> descriptors = getFilteredProperties(configClass);
        for (ConfigurationPropertyImpl property : properties.getProperties()) {
            String name = property.getName();
            PropertyDescriptor desc = descriptors.get(name);
            if (desc == null) {
                final String FMT = "Class ''{0}'' does not have a property ''{1}''.";
                final String MSG = MessageFormat.format(FMT, configClass.getName(), name);
                throw new IllegalArgumentException(MSG);
            }
            Object value = property.getValue();
            //some value types such as arrays
            //are mutable. make sure the config object
            //has its own copy
            value = SerializerUtil.cloneObject(value);
            Method setter = desc.getWriteMethod();
            try {
                setter.invoke(config, value);
            } catch (IllegalArgumentException ex) {
                // just throw if the value is null..
                if (value == null) {
                    throw ex;
                }
                // its probably an argument type mismatch
                // so add information to the response..
                Class<?> expected = setter.getParameterTypes()[0];
                Class<?> actual = value.getClass();
                String fmt = "For property ''{0}'' expected type ''{1}'' actual type ''{2}''.";
                String msg = MessageFormat.format(fmt, name, expected, actual); 
                throw new IllegalArgumentException(msg);
            }
        }
    }
    
    private static Map<String,PropertyDescriptor> 
    getFilteredProperties(Class<? extends Configuration> config)
        throws Exception
    {
        Map<String,PropertyDescriptor> rv = 
            new HashMap<String,PropertyDescriptor>();
        BeanInfo info = Introspector.getBeanInfo(config);
        PropertyDescriptor [] descriptors = info.getPropertyDescriptors();
        for (PropertyDescriptor descriptor : descriptors) {
            String propName = descriptor.getName();
            if ( descriptor.getWriteMethod() == null ) {
                //if there's no setter, ignore it
                continue;
            }
            if ( "connectorMessages".equals(propName) ) {
                // exclude setLocale since its part of the interface..
                continue;
            }
            if ( descriptor.getReadMethod() == null ) {
                final String FMT = 
                    "Found setter ''{0}'' but not the corresponding getter.";
                final String MSG = MessageFormat.format(FMT, propName);
                throw new IllegalArgumentException(MSG);
            }
            rv.put(propName, descriptor);
        }
        return rv;
    }
    
    

    /**
     * Get the option from the property.
     */
    private static ConfigurationProperty getPropertyOptions(
            final Method getter, final Method setter) {
        ConfigurationProperty opts;
        // the setter is dominant place to add the options.
        opts = setter.getAnnotation(ConfigurationProperty.class);
        if (opts == null) {
            // check if they set on the getter..
            opts = getter.getAnnotation(ConfigurationProperty.class);
        }
        return opts;
    }

    
    
}
