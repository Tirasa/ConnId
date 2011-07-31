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

import org.identityconnectors.common.l10n.CurrentLocale;
import org.identityconnectors.framework.common.objects.ConnectorMessages;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;


/**
 * The connector meta-data for a given connector.
 */
public interface ConnectorInfo {

    /**
     * Returns a friendly name suitable for display in the UI.
     * The name will be localized using the {@link CurrentLocale}.
     * 
     * @return The friendly name
     */
    public String getConnectorDisplayName();
    
    /**
     * Returns the connector messages for this connector
     * @return The connector messages for this connector.
     */
    public ConnectorMessages getMessages();

    /**
     * Uniquely identifies this connector in a given installation
     * @return The connector key
     */
    public ConnectorKey getConnectorKey();


    /**
     * Loads the {@link Connector} and {@link Configuration} class in order to
     * determine the proper default configuration parameters.
     */
    public APIConfiguration createDefaultAPIConfiguration();
    
}
