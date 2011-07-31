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

import static org.identityconnectors.common.ReflectionUtil.getInterfaces;

import java.util.List;

import org.identityconnectors.framework.api.operations.APIOperation;


/**
 * Base class for API operation runners.
 */
public abstract class APIOperationRunner {
    
    /**
     * Context that has all the information required to execute an operation.
     */
    private final OperationalContext _context;

    
    /**
     * Creates the API operation so it can called multiple times.
     */
    public APIOperationRunner(final OperationalContext context) {
        _context = context;
        // get the APIOperation that this class implements..
        List<Class<? extends APIOperation>> apiOps = getInterfaces(this
                .getClass(), APIOperation.class);
        // there should be only one..
        if (apiOps.size() > 1) {
            final String MSG = "Must only implement one operation.";
            throw new IllegalStateException(MSG);
        }
    }
    
    /**
     * Get the current operational context.
     */
    public OperationalContext getOperationalContext() {
        return _context;
    }
    
}
