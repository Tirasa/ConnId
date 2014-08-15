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
 * http://opensource.org/licenses/cddl1.php
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://opensource.org/licenses/cddl1.php.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 * Portions Copyrighted 2010-2013 ForgeRock AS.
 * Portions Copyrighted 2014 Evolveum
 */
package org.identityconnectors.framework.impl.api.local.operations;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.operations.ScriptOnResourceApiOp;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.ScriptContext;
import org.identityconnectors.framework.common.serializer.SerializerUtil;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.ScriptOnResourceOp;

public class ScriptOnResourceImpl extends ConnectorAPIOperationRunner implements
        ScriptOnResourceApiOp {
	
	// Special logger with SPI operation log name. Used for logging operation entry/exit
    private static final Log OP_LOG = Log.getLog(ScriptOnResourceOp.class);

    public ScriptOnResourceImpl(final ConnectorOperationalContext context, final Connector connector) {
        super(context, connector);
    }

    @Override
    public Object runScriptOnResource(ScriptContext request, OperationOptions options) {
        Assertions.nullCheck(request, "request");
        // convert null into empty
        if (options == null) {
            options = new OperationOptionsBuilder().build();
        }
        
        if (isLoggable()) {
        	StringBuilder bld = new StringBuilder();
            bld.append("Enter: runScriptOnResource(");
            bld.append(request).append(", ");
            bld.append(options).append(")");
            final String msg = bld.toString();
            OP_LOG.log(ScriptOnResourceOp.class, "runScriptOnResource", SpiOperationLoggingUtil.LOG_LEVEL, msg, null);
        }
        
        Object rv;
        try {
        	rv = ((ScriptOnResourceOp) getConnector()).runScriptOnResource(request, options);
        } catch (RuntimeException e) {
        	SpiOperationLoggingUtil.logOpException(OP_LOG, ScriptOnResourceOp.class, "runScriptOnResource", e);
        	throw e;
        }
        
        if (isLoggable()) {
        	OP_LOG.log(ScriptOnResourceOp.class, "runScriptOnResource", SpiOperationLoggingUtil.LOG_LEVEL,
        			"Return: "+rv, null);
        }
        
        return SerializerUtil.cloneObject(rv);
    }

    private static boolean isLoggable() {
		return OP_LOG.isLoggable(SpiOperationLoggingUtil.LOG_LEVEL);
	}
}
