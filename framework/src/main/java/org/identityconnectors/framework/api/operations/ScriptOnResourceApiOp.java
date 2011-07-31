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

import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ScriptContext;
import org.identityconnectors.framework.common.serializer.ObjectSerializerFactory;
import org.identityconnectors.framework.spi.operations.ScriptOnResourceOp;

/**
 * Runs a script on the target resource that a connector manages.
 * This API operation is supported only for a connector that implements
 * {@link ScriptOnResourceOp}. 
 * <p>
 * The contract here at the API level is intentionally very loose.  
 * Each connector decides what script languages it supports, 
 * what running a script <b>on</b> a target resource actually means, 
 * and what script options (if any) that connector supports.
 * Refer to the javadoc of each particular connector for more information.
 */
public interface ScriptOnResourceApiOp extends APIOperation {
    
    /**
     * Runs a script on a specific target resource.  
     * @param request The script and arguments to run.
     * @param options Additional options which control how the script is
     *  run. Please refer to the connector documentation for supported 
     *  options.
     * @return The result of the script. The return type must be
     * a type that the connector framework supports for serialization.
     * See {@link ObjectSerializerFactory} for a list of supported return types.
     */
    public Object runScriptOnResource(ScriptContext request,
            OperationOptions options);
}
