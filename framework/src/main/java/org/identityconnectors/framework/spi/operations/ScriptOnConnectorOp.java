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
package org.identityconnectors.framework.spi.operations;

import org.identityconnectors.framework.api.operations.ScriptOnConnectorApiOp;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ScriptContext;
import org.identityconnectors.framework.common.serializer.ObjectSerializerFactory;

/**
  * Operation that runs a script in the environment of the connector.
 * (Compare to {@link ScriptOnResourceOp}, which runs a script
 * on the target resource that the connector manages.)
 * A connector that intends to <i>provide to scripts
 * more than is required by the basic contract</i>
 * specified in the javadoc for {@link ScriptOnConnectorApiOp} 
 * should implement this interface.
 * <p>
 * Each connector that implements this interface must support 
 * <em>at least</em> the behavior specified by {@link ScriptOnConnectorApiOp}. 
 * A connector also may expose additional variables for use by scripts
 * and may respond to specific {@link OperationOptions options}. 
 * Each connector that implements this interface 
 * must describe in its javadoc as available "for use by connector scripts"
 * any such additional variables or supported options.
 * <p>
 * <b>NOTE: </b> It will be fairly typical for connectors to not need
 * to implement this method. Connectors may still expose features for
 * a script to use simply by having a method public. Those methods
 * that are intended for a script to be used should be documented
 * as "for use by connector scripts". Otherwise script authors
 * should not assume that the method is safe to be called from a script.
 */
public interface ScriptOnConnectorOp extends SPIOperation {
    
    /**
     * Runs the script request.  
     * @param request The script and arguments to run.
     * @param options Additional options that control how the script is
     *  run. 
     * @return The result of the script. The return type must be
     * a type that the framework supports for serialization.
     * See {@link ObjectSerializerFactory} for a list of supported types.
     */
    public Object runScriptOnConnector(ScriptContext request,
            OperationOptions options);
}
