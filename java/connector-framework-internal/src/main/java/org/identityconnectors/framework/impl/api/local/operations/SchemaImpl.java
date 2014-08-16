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
 * Portions Copyrighted 2014 Evolveum
 */
package org.identityconnectors.framework.impl.api.local.operations;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.SchemaOp;

public class SchemaImpl extends ConnectorAPIOperationRunner implements
        org.identityconnectors.framework.api.operations.SchemaApiOp {
	
	// Special logger with SPI operation log name. Used for logging operation entry/exit
    private static final Log OP_LOG = Log.getLog(SchemaOp.class);
	
    /**
     * Initializes the operation works.
     */
    public SchemaImpl(final ConnectorOperationalContext context,
            final Connector connector) {
        super(context,connector);
    }

    /**
     * Retrieve the schema from the {@link Connector}.
     *
     * @see org.identityconnectors.framework.api.operations.SchemaApiOp#schema()
     */
    @Override
    public Schema schema() {
    	
    	if (isLoggable()) {
            OP_LOG.log(SchemaOp.class, "schema", SpiOperationLoggingUtil.LOG_LEVEL, "Enter: schema()", null);
        }
        
    	Schema schema;
    	try {
    		schema = ((SchemaOp)getConnector()).schema();
    	} catch (RuntimeException e) {
    		SpiOperationLoggingUtil.logOpException(OP_LOG, SchemaOp.class, "schema", e);
        	throw e;
    	}
    	
    	if (isLoggable()) {
        	OP_LOG.log(SchemaOp.class, "schema", SpiOperationLoggingUtil.LOG_LEVEL,
        			"Return: "+schema, null);
        }
    	
    	return schema;
    }
    
    private static boolean isLoggable() {
		return OP_LOG.isLoggable(SpiOperationLoggingUtil.LOG_LEVEL);
	}
}
