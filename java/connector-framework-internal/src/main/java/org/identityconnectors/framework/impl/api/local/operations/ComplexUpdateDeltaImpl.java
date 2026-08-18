/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 ConnId. All rights reserved.
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
import org.identityconnectors.framework.api.operations.ComplexUpdateDeltaApiOp;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.ComplexUpdateDeltaOp;
import org.identityconnectors.framework.spi.operations.UpdateDeltaOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;

import java.util.Set;

public class ComplexUpdateDeltaImpl extends ConnectorAPIOperationRunner implements ComplexUpdateDeltaApiOp {

    // Special logger with SPI operation log name. Used for logging operation entry/exit
    private static final Log OP_LOG = Log.getLog(ComplexUpdateDeltaOp.class);

    public ComplexUpdateDeltaImpl(ConnectorOperationalContext context, Connector connector) {
        super(context, connector);
    }

    @Override
    public Set<BaseAttributeDelta> complexUpdateDelta(ObjectClass objclass, Uid uid, Set<BaseAttributeDelta> modifications, OperationOptions options) {
        var connector = getConnector();
        if (connector instanceof ComplexUpdateDeltaOp) {
        ComplexUpdateDeltaOp deltaOp = (ComplexUpdateDeltaOp) connector;

            logOpEntry("complexUpdateDelta", objclass, uid, modifications, options);

            Set<BaseAttributeDelta> attrsDelta;
            try {
                attrsDelta = deltaOp.complexUpdateDelta(objclass, uid, modifications, options);
            } catch (RuntimeException e) {
                logOpException("complexUpdateDelta", e);
                throw e;
            }
            logOpExit("complexUpdateDelta", attrsDelta);
        }
        // FIXME: Add correct exceptions
        throw new UnsupportedOperationException();
    }

    private void logOpEntry(String opName, Object... params) {
        SpiOperationLoggingUtil.logOpEntry(OP_LOG, getOperationalContext(), ComplexUpdateDeltaOp.class, opName, params);
    }

    private void logOpExit(String opName, Object returnValue) {
        SpiOperationLoggingUtil.logOpExit(OP_LOG, getOperationalContext(), ComplexUpdateDeltaOp.class, opName, returnValue);
    }

    private void logOpException(String opName, RuntimeException e) {
        SpiOperationLoggingUtil.logOpException(OP_LOG, getOperationalContext(), ComplexUpdateDeltaOp.class, opName, e);
    }
}
