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
package org.identityconnectors.framework.api.operations;

import org.identityconnectors.framework.api.APIConfiguration;

/**
 * Validates the {@link APIConfiguration configuration}.
 * 
 * <p>A valid configuration is one that is ready to be used by the connector:
 * it is complete (all the required properties have been given values) 
 * and the property values are well-formed (are in the expected range, 
 * have the expected format, etc.)</p>
 */
public interface ValidateApiOp extends APIOperation {

    /**
     * Validates the {@link APIConfiguration configuration}.
     * 
     * @throws RuntimeException
     *             iff the configuration is not valid.
     */
    void validate();
}
