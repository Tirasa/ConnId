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
 */
package org.identityconnectors.framework.common.exceptions;

/**
 * Thrown when an
 * {@link org.identityconnectors.framework.api.operations.APIOperation
 * operation} <i>times out</i>. The framework cancels an operation when the
 * corresponding method has been executing for longer than the
 * {@link org.identityconnectors.framework.api.APIConfiguration#setTimeout limit
 * specified in APIConfiguration}.
 */
public class OperationTimeoutException extends ConnectorException {

    private static final long serialVersionUID = 1L;

    /**
     * @see ConnectorException#ConnectorException()
     */
    public OperationTimeoutException() {
        super();
    }

    /**
     * @see ConnectorException#ConnectorException(String)
     */
    public OperationTimeoutException(String msg) {
        super(msg);
    }

    /**
     * @see ConnectorException#ConnectorException(Throwable)
     */
    public OperationTimeoutException(Throwable e) {
        super(e);
    }

    /**
     * @see ConnectorException#ConnectorException(String, Throwable)
     */
    public OperationTimeoutException(String msg, Throwable e) {
        super(msg, e);
    }

}
