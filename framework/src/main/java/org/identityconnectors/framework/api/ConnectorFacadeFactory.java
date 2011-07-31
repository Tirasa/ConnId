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

import org.identityconnectors.framework.common.exceptions.ConnectorException;

/**
 * Allows an application to obtain a {@link ConnectorFacade connector instance}.
 * Manages a pool of connector instances.
 * 
 * @author Will Droste
 * @version $Revision $
 * @since 1.0
 */
public abstract class ConnectorFacadeFactory {

    // At some point we might make this pluggable, but for now, hard-code
    private static final String IMPL_NAME = "org.identityconnectors.framework.impl.api.ConnectorFacadeFactoryImpl";

    private static ConnectorFacadeFactory _instance;

    /**
     * Get the singleton instance of the {@link ConnectorFacadeFactory}.
     */
    public static synchronized ConnectorFacadeFactory getInstance() {
        if (_instance == null) {
            try {
                Class<?> clazz = Class.forName(IMPL_NAME);
                Object object = clazz.newInstance();
                _instance = ConnectorFacadeFactory.class.cast(object);
            } catch (Exception e) {
                throw ConnectorException.wrap(e);
            }
        }
        return _instance;
    }

    /**
     * Dispose of all connector pools, resources, etc.
     */
    public abstract void dispose();

    /**
     * Get a new instance of {@link ConnectorFacade}.
     * 
     * @param config
     *            all the configuration that the framework, connector, and
     *            pooling needs.
     * @return {@link ConnectorFacade} to call API operations against.
     */
    public abstract ConnectorFacade newInstance(APIConfiguration config);

}
