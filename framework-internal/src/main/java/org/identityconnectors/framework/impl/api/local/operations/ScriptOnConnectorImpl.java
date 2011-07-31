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

import java.util.HashMap;
import java.util.Map;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.script.ScriptExecutor;
import org.identityconnectors.common.script.ScriptExecutorFactory;
import org.identityconnectors.framework.api.operations.ScriptOnConnectorApiOp;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.ScriptContext;
import org.identityconnectors.framework.common.serializer.SerializerUtil;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.ScriptOnConnectorOp;


public class ScriptOnConnectorImpl extends ConnectorAPIOperationRunner
        implements ScriptOnConnectorApiOp {
    
    public ScriptOnConnectorImpl(final ConnectorOperationalContext context,
            final Connector connector) {
        super(context,connector);
    }

    public Object runScriptOnConnector(ScriptContext request,
            OperationOptions options) {
        Assertions.nullCheck(request, "request");
        //convert null into empty
        if ( options == null ) {
            options = new OperationOptionsBuilder().build();
        }
        Object rv;
        if ( getConnector() instanceof ScriptOnConnectorOp ) {
            rv = ((ScriptOnConnectorOp)getConnector()).runScriptOnConnector(request, options);
        }
        else {
            String language = request.getScriptLanguage();
            ClassLoader classloader =
                getConnector().getClass().getClassLoader();
            ScriptExecutor executor = 
                ScriptExecutorFactory.newInstance(language).newScriptExecutor(classloader,
                        request.getScriptText(),
                        false);
            Map<String,Object> scriptArgs = new HashMap<String,Object>();
            scriptArgs.putAll(request.getScriptArguments()); //add the args passed by the application
            scriptArgs.put("connector",getConnector()); //add the connector instance itself
            try {
                rv = executor.execute(scriptArgs); 
            }
            catch (Exception e) {
                throw ConnectorException.wrap(e);
            }
        }
        return SerializerUtil.cloneObject(rv);
    }

}
