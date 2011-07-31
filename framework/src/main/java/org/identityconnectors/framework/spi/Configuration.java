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
package org.identityconnectors.framework.spi;

import org.identityconnectors.framework.common.FrameworkUtil;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.objects.ConnectorMessages;
import org.identityconnectors.framework.spi.operations.TestOp;

/**
 * Encapsulates the configuration of a connector.
 * 
 * <p>Implementations of the <code>Configuration</code> interface must have a default
 * constructor. All bean properties are considered configuration for the connector.
 * The initial value of the property getter method is
 * considered the default value of the property. The types of the bean properties
 * can be only those returned by {@link FrameworkUtil#getAllSupportedConfigTypes()} and
 * multi-dimensional arrays thereof. The bean properties are not required by default,
 * but a property can be marked as required through use of the {@link ConfigurationProperty} annotation.</p>
 *   
 * <p>Each bean property corresponds to two keys in a
 * properties file named <code>Messages</code> in the same package as the implementing class:
 * <code>${property}.display</code> and <code>${property}.help</code>. For example,
 * <code>hostname.help</code> and <code>hostname.display</code> would be the keys
 * corresponding to a <code>hostname</code> property. The <code>display</code> message is the display
 * name of the property and can be used to display the property in a view. The <code>help</code>
 * message holds the description of the property. The names of the two keys can be overridden
 * through the <code>ConfigurationProperty</code> annotation.</p>
 */
public interface Configuration {

    /**
     * Determines if the configuration is valid.
     * 
     * <p>A valid configuration is one that is ready to be used by the connector:
     * it is complete (all the required properties have been given values) 
     * and the property values are well-formed (are in the expected range, 
     * have the expected format, etc.)</p>
     * 
     * <p>Implementations of this method <strong>should not</strong> connect to the resource
     * in an attempt to validate the configuration. For example, implementations
     * should not attempt to check that a host of the specified name exists
     * by making a connection to it. Such checks can be performed in the implementation
     * of the {@link TestOp#test()} method.</p>
     * 
     * @throws RuntimeException iff the configuration is not valid. Implementations
     *             are encouraged to throw the most specific exception available.
     *             When no specific exception is available, implementations can throw
     *             {@link ConfigurationException}.
     */
    public void validate();

    /**
     * Should return the {@link ConnectorMessages message catalog} that is set by
     * {@link #setConnectorMessages(ConnectorMessages)}.
     * 
     * @return the <code>ConnectorMessages</code> instance.
     */
    public ConnectorMessages getConnectorMessages();
        
    /**
     * Sets the {@link ConnectorMessages message catalog} instance that allows the Connector
     * to localize messages. This method is called before any bean property setter,
     * the {@link #validate()} method or the {@link #getConnectorMessages()} method. 
     * 
     * @param messages
     *             the message catalog.
     */
    public void setConnectorMessages(ConnectorMessages messages);
}
