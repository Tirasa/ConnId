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
package org.identityconnectors.framework.api;

import java.util.List;

import org.identityconnectors.framework.spi.Configuration;


/**
 * Encapsulates the {@link Configuration Configuration at the SPI layer} and
 * uses reflection to identify the individual properties 
 * that are available for an application to manipulate.
 */
public interface ConfigurationProperties {

    /**
     * Get the list of properties names for this {@link Configuration}.
     * 
     * @return a list containing the names of properties 
     * that an application can configure for a connector.
     */
    List<String> getPropertyNames();

    /**
     * Get a particular {@link ConfigurationProperty} by name.
     * 
     * @param name
     *            the unique name of the property.
     * @return a {@link ConfigurationProperty} if it exists otherwise null.
     */
    ConfigurationProperty getProperty(String name);

    /**
     * Set the value of the {@link Configuration} property by name.
     * 
     * @param name
     *            Name of the property to set the value against.
     * @param value
     *            Value to set on the configuration property.
     * @throws IllegalArgumentException
     *             iff the property name does not exist.
     */
    void setPropertyValue(String name, Object value);

}
