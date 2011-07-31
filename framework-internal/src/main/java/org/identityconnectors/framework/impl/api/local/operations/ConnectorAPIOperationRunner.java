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

import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.spi.AttributeNormalizer;
import org.identityconnectors.framework.spi.Connector;

/**
 * Subclass of APIOperationRunner for operations that require a connector.
 */
public abstract class ConnectorAPIOperationRunner extends APIOperationRunner {

    /**
     * The connector instance
     */
    private final Connector _connector;
    
    /**
     * Creates the API operation so it can called multiple times.
     */
    public ConnectorAPIOperationRunner(final ConnectorOperationalContext context,
                              final Connector connector) {
        super(context);
        _connector = connector;
    }
    
    /**
     * Get the current operational context.
     */
    @Override
    public ConnectorOperationalContext getOperationalContext() {
        return (ConnectorOperationalContext)super.getOperationalContext();
    }
    
    public Connector getConnector() {
        return _connector;
    }
    
    public final ObjectNormalizerFacade getNormalizer(ObjectClass oclass) {
        AttributeNormalizer norm = null;
        Connector connector = getConnector();
        if ( connector instanceof AttributeNormalizer ) {
            norm = (AttributeNormalizer)connector;
        }
        return new ObjectNormalizerFacade(oclass,norm);
    }
}
