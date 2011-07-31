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
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.ScriptOnConnectorOp;

/**
 * Runs a script in the same JVM or .Net Runtime as the <code>Connector</code>.
 * That is, if you are using a <b>local</b> framework, the script will be
 * run in your JVM. If you are connected to a <b>remote</b> framework, the
 * script will be run in the remote JVM or .Net Runtime.
 * <p>
 * This API allows an application to run a script in the context
 * of any connector.  (A connector need not implement any particular interface
 * in order to enable this.)  The <b>minimum contract</b> to which each connector
 * <b>must</b> adhere is as follows:
 * <ol>
 *    <li>Script will run in the same classloader/execution environment
 *    as the connector, so the script will have access to all the classes
 *    to which the connector has access.
 *    </li>
 *    <li>Script will have access to a <code>"connector"</code> variable 
 *    that is equivalent to an initialized instance of a connector. 
 *    Thus, at a minimum the script will be able to access 
 *    {@link Connector#getConfiguration() the configuration of the connector}.
 *    </li>
 *    <li>Script will have access to any 
 *    {@link ScriptContext#getScriptArguments() script-arguments}
 *    passed in by the application.
 *    </li>
 * </ol>
 * <p>
 * A connector that implements {@link ScriptOnConnectorOp} 
 * may provide more variables than what is described above. 
 * A connector also may perform special processing
 * for {@link OperationOptions} specific to that connector.
 * Consult the javadoc of each particular connector to find out what
 * additional capabilities, if any, that connector exposes for use in scripts. 
 * <p>
 * <b>NOTE:</b> A caller who wants to execute scripts on a connector
 * should assume that <em>a script must not use any method of the connector 
 * beyond the minimum contract described above</em>,
 * unless the connector explicitly documents that method as 
 * "for use by connector script".  The primary function of a connector 
 * is to implement the SPI in the context of the Connector framework.  
 * In general, no caller should invoke Connector methods directly
 * --whether by a script or by other means.
 */
public interface ScriptOnConnectorApiOp extends APIOperation {
    
    /**
     * Runs the script.  
     * @param request - The script and arguments to run.
     * @param options - Additional options that control how the script is
     *  run. The framework does not currently recognize any options
     *  but specific connectors might. Consult the documentation
     *  for each connector to identify supported options.
     * @return The result of the script. The return type must be
     * a type that the framework supports for serialization.
     * @see ObjectSerializerFactory for a list of supported return types.
     */
    public Object runScriptOnConnector(ScriptContext request,
            OperationOptions options);
}
