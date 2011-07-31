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
package org.identityconnectors.framework.impl.api.local.operations;

import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.local.JavaClassProperties;
import org.identityconnectors.framework.impl.api.local.LocalConnectorInfoImpl;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;


/**
 * OperationalContext - base class for operations that do not
 * require a connector instance.
 */
public class OperationalContext {
    
    /**
     * ConnectorInfo
     */
    private final LocalConnectorInfoImpl connectorInfo;
    
    /**
     * Contains the {@link Connector} {@link Configuration}.
     */
    private final APIConfigurationImpl apiConfiguration;
    

    public OperationalContext(final LocalConnectorInfoImpl connectorInfo,
            final APIConfigurationImpl apiConfiguration) {
        this.connectorInfo = connectorInfo;
        this.apiConfiguration = apiConfiguration;
    }

    public Configuration getConfiguration() {
        return JavaClassProperties.createBean(this.apiConfiguration.getConfigurationProperties(), 
                connectorInfo.getConnectorConfigurationClass());
    }
    
    protected LocalConnectorInfoImpl getConnectorInfo() {
        return connectorInfo;
    }
}
