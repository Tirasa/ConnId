/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2025 Evolveum. All rights reserved.
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
 */
package org.identityconnectors.framework.impl.api.local.operations;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.operations.PartialSchemaApiOp;
import org.identityconnectors.framework.api.operations.SchemaApiOp;
import org.identityconnectors.framework.common.objects.LightweightObjectClassInfo;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.PartialSchemaOp;

public class PartialSchemaImpl extends ConnectorAPIOperationRunner implements PartialSchemaApiOp {

    // Special logger with SPI operation log name. Used for logging operation entry/exit
    private static final Log OP_LOG = Log.getLog(PartialSchemaOp.class);

    /**
     * Initializes the operation works.
     */
    public PartialSchemaImpl(final ConnectorOperationalContext context, final Connector connector) {
        super(context, connector);
    }

    /**
     * Retrieve the schema from the {@link Connector}.
     *
     * @see SchemaApiOp#schema()
     */


    @Override
    public Schema getPartialSchema(LightweightObjectClassInfo... ObjectClassInfo) {
        SpiOperationLoggingUtil.logOpEntry(OP_LOG, getOperationalContext(), PartialSchemaOp.class, "getPartialSchema");

        Schema partialSchema;
        try {
            partialSchema = ((PartialSchemaOp) getConnector()).getPartialSchema(ObjectClassInfo);
        } catch (RuntimeException e) {
            SpiOperationLoggingUtil.logOpException(OP_LOG, getOperationalContext(), PartialSchemaOp.class, "getPartialSchema", e);
            throw e;
        }

        SpiOperationLoggingUtil.logOpExit(OP_LOG, getOperationalContext(), PartialSchemaOp.class, "getPartialSchema", partialSchema);

        return partialSchema;
    }

    @Override
    public LightweightObjectClassInfo[] getObjectClassInformation() {
        {
            SpiOperationLoggingUtil.logOpEntry(OP_LOG, getOperationalContext(), PartialSchemaOp.class, "getObjectClassInformation");

            LightweightObjectClassInfo[] objectClassInfos;
            try {
                objectClassInfos = ((PartialSchemaOp) getConnector()).getObjectClassInformation();
            } catch (RuntimeException e) {
                SpiOperationLoggingUtil.logOpException(OP_LOG, getOperationalContext(), PartialSchemaOp.class, "getObjectClassInformation", e);
                throw e;
            }

            SpiOperationLoggingUtil.logOpExit(OP_LOG, getOperationalContext(), PartialSchemaOp.class, "getObjectClassInformation", objectClassInfos);

            return objectClassInfos;
        }
    }
}
