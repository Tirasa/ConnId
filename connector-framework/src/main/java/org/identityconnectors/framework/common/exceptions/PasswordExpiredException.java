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

import org.identityconnectors.framework.api.operations.AuthenticationApiOp;
import org.identityconnectors.framework.common.objects.Uid;

/**
 * Thrown when a password credential is invalid.
 */
public class PasswordExpiredException extends InvalidPasswordException {

    private static final long serialVersionUID = 1L;

    private transient Uid uid;

    /**
     * @see ConnectorException#ConnectorException()
     */
    public PasswordExpiredException() {
        super();
    }

    /**
     * @see ConnectorException#ConnectorException(String)
     */
    public PasswordExpiredException(String message) {
        super(message);
    }

    /**
     * @see ConnectorException#ConnectorException(Throwable)
     */
    public PasswordExpiredException(Throwable ex) {
        super(ex);
    }

    /**
     * @see ConnectorException#ConnectorException(String, Throwable)
     */
    public PasswordExpiredException(String message, Throwable ex) {
        super(message, ex);
    }

    public Uid getUid() {
        return uid;
    }

    /**
     * Sets the Uid. Connectors who throw this exception from their
     * {@link AuthenticationApiOp} should set the account Uid if available.
     *
     * @param uid
     *            The uid.
     * @return A reference to this.
     */
    public PasswordExpiredException initUid(Uid uid) {
        this.uid = uid;
        return this;
    }
}
