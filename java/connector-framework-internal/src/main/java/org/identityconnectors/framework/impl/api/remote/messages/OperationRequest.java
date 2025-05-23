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
 */
package org.identityconnectors.framework.impl.api.remote.messages;

import java.util.List;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.api.operations.APIOperation;

/**
 * Sent to request an operation. Response will consist of one-or-more
 * {@link OperationResponsePart}'s followed by an {@link OperationResponseEnd}.
 */
public class OperationRequest implements Message {

    /**
     * The key of the connector to operate on.
     */
    private final ConnectorKey connectorKey;

    /**
     * The configuration information to use.
     */
    private final String configuration;

    /**
     * The operation to perform.
     */
    private final Class<? extends APIOperation> operation;

    /**
     * The name of the method since operations can have more than one method.
     * NOTE: this is case-insensitive
     */
    private final String operationMethodName;

    /**
     * The arguments to the operation. In general, these correspond to the
     * actual arguments of the method. The one exception is search - in this
     * case, the callback is not passed.
     */
    private final List<Object> arguments;

    public OperationRequest(ConnectorKey key, String connectorFacadeKey,
            Class<? extends APIOperation> operation, String operationMethodName,
            List<Object> arguments) {
        connectorKey = key;
        configuration = connectorFacadeKey;
        this.operation = operation;
        this.operationMethodName = operationMethodName;
        this.arguments = CollectionUtil.newReadOnlyList(arguments);
    }

    public ConnectorKey getConnectorKey() {
        return connectorKey;
    }

    public String getConnectorFacadeKey() {
        return configuration;
    }

    public Class<? extends APIOperation> getOperation() {
        return operation;
    }

    public String getOperationMethodName() {
        return operationMethodName;
    }

    public List<Object> getArguments() {
        return arguments;
    }
}
