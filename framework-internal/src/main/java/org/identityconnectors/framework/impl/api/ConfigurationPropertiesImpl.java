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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConfigurationProperty;


public class ConfigurationPropertiesImpl implements ConfigurationProperties {

    // =======================================================================
    // Fields
    // =======================================================================
    /**
     * Properties, listed in order by their "order" attribute
     */
    LinkedHashMap<String, ConfigurationPropertyImpl> _properties;

    /**
     * The container. Not serialized in this object. Set when this
     * property is added to parent
     */
    private transient APIConfigurationImpl _parent;
    
    // =======================================================================
    // Internal Methods
    // =======================================================================
        
    public APIConfigurationImpl getParent() {
        return _parent;
    }
    
    public void setParent(APIConfigurationImpl parent) {
        _parent = parent;
    }
    
    public void setProperties(Collection<ConfigurationPropertyImpl> in) {
        List<ConfigurationPropertyImpl> properties =
           new ArrayList<ConfigurationPropertyImpl>(in);
        Collections.sort(properties, new Comparator<ConfigurationPropertyImpl>() {
            public int compare(ConfigurationPropertyImpl o1,
                    ConfigurationPropertyImpl o2) {
                int or1 = o1.getOrder();
                int or2 = o2.getOrder();
                return or1 < or2 ? -1 : or1 > or2 ? 1 : 0;
            }
        });
        LinkedHashMap<String, ConfigurationPropertyImpl> temp =
            new LinkedHashMap<String, ConfigurationPropertyImpl>();
        for (ConfigurationPropertyImpl property : properties) {
            temp.put(property.getName(), property);
            property.setParent(this);
        }
        _properties = temp;
    }
    
    public Collection<ConfigurationPropertyImpl> getProperties() {
        return _properties.values();
    }
    
        
    // =======================================================================
    // Interface Methods
    // =======================================================================
    /**
     * {@inheritDoc}
     */
    public ConfigurationProperty getProperty(String name) {
        return _properties.get(name);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getPropertyNames() {
        List<String> names = new ArrayList<String>(_properties.keySet());
        return CollectionUtil.newReadOnlyList(names);
    }

    /**
     * {@inheritDoc}
     */
    public void setPropertyValue(String name, Object value) {
        ConfigurationPropertyImpl property = _properties.get(name);
        if (property == null) {
            final String TMPL = "Property ''{0}'' does not exist.";
            final String MSG = MessageFormat.format(TMPL, name);
            throw new IllegalArgumentException(MSG);
        }
        property.setValue(value);
    }
    
    public boolean equals(Object o) {
        if ( o instanceof ConfigurationPropertiesImpl ) {
            ConfigurationPropertiesImpl other = (ConfigurationPropertiesImpl)o;
            HashSet<ConfigurationPropertyImpl> set1 = 
                new HashSet<ConfigurationPropertyImpl>(
                        _properties.values());
            HashSet<ConfigurationPropertyImpl> set2 = 
                new HashSet<ConfigurationPropertyImpl>(
                        other._properties.values());
            return set1.equals(set2);
        }
        return false;
    }
    
    public int hashCode() {
        HashSet<ConfigurationPropertyImpl> set1 = 
            new HashSet<ConfigurationPropertyImpl>(
                    _properties.values());
        return set1.hashCode();
    }
}
