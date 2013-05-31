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
 */
package org.identityconnectors.framework.impl.api;

import java.util.Set;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.api.ConfigurationProperty;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.common.objects.ConnectorMessages;

public class ConfigurationPropertyImpl implements ConfigurationProperty {

    // =======================================================================
    // Fields
    // =======================================================================

    /**
     * Order the property should be displayed.
     */
    private int order;

    /**
     * Is this a confidential property?
     */
    private boolean confidential;

    /**
     * Unique name of the property.
     */
    private String name;

    /**
     * Help message key.
     */
    private String helpMessageKey;

    /**
     * Display message key.
     */
    private String displayMessageKey;

    /**
     * Group message key.
     */
    private String groupMessageKey;

    /**
     * The value of the property
     */
    private Object value;

    /**
     * The type of this property
     */
    private Class<?> type;

    /**
     * The set of operations for which this property applies
     */
    private Set<Class<? extends APIOperation>> operations;

    /**
     * Is this property required?
     */
    private boolean required;

    /**
     * The container. Not serialized in this object. Set when this property is
     * added to parent
     */
    private transient ConfigurationPropertiesImpl parent;

    // =======================================================================
    // Internal Methods
    // =======================================================================
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setConfidential(boolean confidential) {
        this.confidential = confidential;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHelpMessageKey() {
        return helpMessageKey;
    }

    public void setHelpMessageKey(String key) {
        helpMessageKey = key;
    }

    public String getDisplayMessageKey() {
        return displayMessageKey;
    }

    public void setDisplayMessageKey(String key) {
        displayMessageKey = key;
    }

    public String getGroupMessageKey() {
        return groupMessageKey;
    }

    public void setGroupMessageKey(String key) {
        groupMessageKey = key;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public ConfigurationPropertiesImpl getParent() {
        return parent;
    }

    public void setParent(ConfigurationPropertiesImpl parent) {
        this.parent = parent;
    }

    public Set<Class<? extends APIOperation>> getOperations() {
        return operations;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean v) {
        required = v;
    }

    public void setOperations(Set<Class<? extends APIOperation>> set) {
        operations = CollectionUtil.newReadOnlySet(set);
    }

    private String formatMessage(String key, String dflt, Object... args) {
        APIConfigurationImpl apiConfig = getParent().getParent();
        ConnectorMessages messages = apiConfig.getConnectorInfo().getMessages();
        return messages.format(key, dflt, args);
    }

    // =======================================================================
    // Interface Methods
    // =======================================================================

    /**
     * {@inheritDoc}
     */
    public boolean isConfidential() {
        return confidential;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    public Object getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    public String getHelpMessage(String def) {
        return formatMessage(helpMessageKey, def);
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName(String def) {
        return formatMessage(displayMessageKey, def);
    }

    /**
     * {@inheritDoc}
     */
    public String getGroup(String def) {
        return formatMessage(groupMessageKey, def);
    }

    public int hashCode() {
        return getName().hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof ConfigurationPropertyImpl) {
            ConfigurationPropertyImpl other = (ConfigurationPropertyImpl) o;
            if (!getName().equals(other.getName())) {
                return false;
            }
            if (!CollectionUtil.equals(getValue(), other.getValue())) {
                return false;
            }
            if (getOrder() != other.getOrder()) {
                return false;
            }
            if (!CollectionUtil.equals(getHelpMessageKey(), other.getHelpMessageKey())) {
                return false;
            }
            if (!CollectionUtil.equals(getDisplayMessageKey(), other.getDisplayMessageKey())) {
                return false;
            }
            if (!CollectionUtil.equals(getGroupMessageKey(), other.getGroupMessageKey())) {
                return false;
            }
            if (isConfidential() != other.isConfidential()) {
                return false;
            }
            if (isRequired() != other.isRequired()) {
                return false;
            }
            if (!CollectionUtil.equals(getType(), other.getType())) {
                return false;
            }
            if (!CollectionUtil.equals(operations, other.operations)) {
                return false;
            }

            return true;
        }
        return false;
    }

}
