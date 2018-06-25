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
 * Portions Copyrighted 2014 ForgeRock AS.
 * Portions Copyrighted 2014-2018 Evolveum
 * Portions Copyrighted 2018 ConnId
 */
package org.identityconnectors.framework.impl.api.local.operations;

import org.identityconnectors.common.Assertions;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.operations.AuthenticationApiOp;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.operations.AuthenticateOp;

public class AuthenticationImpl extends ConnectorAPIOperationRunner implements AuthenticationApiOp {

    // Special logger with SPI operation log name. Used for logging operation entry/exit
    private static final Log OP_LOG = Log.getLog(AuthenticateOp.class);

    /**
     * Pass the configuration etc to the abstract class.
     */
    public AuthenticationImpl(final ConnectorOperationalContext context, final Connector connector) {
        super(context, connector);
    }

    /**
     * Authenticate using the basic credentials.
     *
     * @see AuthenticationOpTests#authenticate(String, String)
     */
    @Override
    public Uid authenticate(
            final ObjectClass objectClass,
            final String username,
            final GuardedString password,
            OperationOptions options) {

        Assertions.nullCheck(objectClass, "objectClass");
        if (ObjectClass.ALL.equals(objectClass)) {
            throw new UnsupportedOperationException(
                    "Operation is not allowed on __ALL__ object class");
        }
        Assertions.nullCheck(username, "username");
        Assertions.nullCheck(password, "password");
        // cast null as empty
        if (options == null) {
            options = new OperationOptionsBuilder().build();
        }

        // Password is GuardedString. toString() method should be safe.
        SpiOperationLoggingUtil.logOpEntry(OP_LOG, getOperationalContext(), AuthenticateOp.class, "authenticate",
                objectClass, username, password, options);

        Uid uid;
        try {
            uid = ((AuthenticateOp) getConnector()).authenticate(objectClass, username, password, options);
        } catch (RuntimeException e) {
            SpiOperationLoggingUtil.
                    logOpException(OP_LOG, getOperationalContext(), AuthenticateOp.class, "authenticate", e);
            throw e;
        }

        SpiOperationLoggingUtil.logOpExit(OP_LOG, getOperationalContext(), AuthenticateOp.class, "authenticate", uid);

        return uid;
    }
}
