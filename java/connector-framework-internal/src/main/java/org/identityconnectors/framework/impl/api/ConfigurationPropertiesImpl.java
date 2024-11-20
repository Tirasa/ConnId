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
 * Portions Copyrighted 2024 ConnId
 */
package org.identityconnectors.framework.impl.api;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConfigurationProperty;

public class ConfigurationPropertiesImpl implements ConfigurationProperties {

    private static final String MSG = "Property ''{0}'' does not exist.";

    // =======================================================================
    // Fields
    // =======================================================================
    /**
     * Properties, listed in order by their "order" attribute
     */
    private Map<String, ConfigurationPropertyImpl> properties;

    /**
     * The container. Not serialized in this object. Set when this property is added to parent
     */
    private transient APIConfigurationImpl parent;

    // =======================================================================
    // Internal Methods
    // =======================================================================
    public APIConfigurationImpl getParent() {
        return parent;
    }

    public void setParent(final APIConfigurationImpl parent) {
        this.parent = parent;
    }

    public void setProperties(final Collection<ConfigurationPropertyImpl> in) {
        List<ConfigurationPropertyImpl> props = in.stream().
                sorted(Comparator.comparing(ConfigurationPropertyImpl::getOrder)).
                collect(Collectors.toList());

        Map<String, ConfigurationPropertyImpl> temp = new LinkedHashMap<>();
        props.forEach(prop -> {
            temp.put(prop.getName(), prop);
            prop.setParent(this);
        });
        this.properties = temp;
    }

    public Collection<ConfigurationPropertyImpl> getProperties() {
        return properties.values();
    }

    // =======================================================================
    // Interface Methods
    // =======================================================================
    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationProperty getProperty(final String name) {
        return properties.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getPropertyNames() {
        List<String> names = new ArrayList<>(properties.keySet());
        return CollectionUtil.newReadOnlyList(names);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPropertyValue(final String name, final Object value) {
        ConfigurationPropertyImpl property = Optional.ofNullable(properties.get(name)).
                orElseThrow(() -> new IllegalArgumentException(MessageFormat.format(MSG, name)));
        property.setValue(value);
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof ConfigurationPropertiesImpl) {
            ConfigurationPropertiesImpl other = (ConfigurationPropertiesImpl) o;
            Set<ConfigurationPropertyImpl> set1 = new HashSet<>(properties.values());
            Set<ConfigurationPropertyImpl> set2 = new HashSet<>(other.properties.values());
            return set1.equals(set2);
        }
        return false;
    }

    @Override
    public int hashCode() {
        Set<ConfigurationPropertyImpl> set1 = new HashSet<>(properties.values());
        return set1.hashCode();
    }
}
