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
package org.identityconnectors.framework.api;

import java.util.Set;

import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.common.objects.SuggestedValues;

/**
 * Represents at the API level a property of a Connector's
 * {@link org.identityconnectors.framework.spi.Configuration Configuration at
 * the SPI layer}.
 */
public interface ConfigurationProperty {

    /**
     * Get the unique name of the configuration property.
     */
    public String getName();

    /**
     * Get the help message from the message catalog.
     */
    public String getHelpMessage(String def);

    /**
     * Get the display name for this configuration property.
     */
    public String getDisplayName(String def);

    /**
     * Get name of the group for this configuration property.
     */
    public String getGroup(String def);

    /**
     * Get the value from the property. This value should be the default value.
     */
    public Object getValue();

    /**
     * Set the value of the property.
     */
    public void setValue(Object o);

    /**
     * Get the type of the property.
     */
    public Class<?> getType();

    /**
     * Is this a confidential property whose value should be encrypted by the
     * application when persisted?
     */
    public boolean isConfidential();

    /**
     * Is this a required property
     *
     * @return True if the property is required
     */
    public boolean isRequired();

    /**
     * Set of operations for which this property must be specified.
     *
     * This is used for the case where a connector may or may not implement
     * certain operations depending in the configuration. The default value of
     * "empty array" is special in that it means that this property is
     * applicable to all operations.
     */
    public Set<Class<? extends APIOperation>> getOperations();

    /**
     * Returns list of allowed values for the property.
     * If a closed list is returned, values specified in the list are the only valid values for the property.
     * Any other value is invalid.
     * If an open list is returned, the values specified in the list should be considered suggestions only.
     * Even an unlisted value can be specified as a valid value of the property.
     * If null is returned, there are no restrictions or suggestions for this property.
     *
     * @since 1.5.2.0
     */
    public SuggestedValues getAllowedValues();
}
