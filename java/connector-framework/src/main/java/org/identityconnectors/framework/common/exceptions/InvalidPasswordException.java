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
 * Thrown when a password credential is invalid.
 * <p>
 * This exception should be used only when incorrect password is entered by a user.
 * It should NOT be used in case that connection to the resource cannot be established
 * because connection password is wrong. Use ConnectionFailedException instead.
 * <p>
 * This exception should be used only when incorrect credential is entered by a user,
 * for example in authenticate() method. It should NOT be used in case that connection
 * to the resource cannot be established because connection credentials are wrong.
 * Use ConnectionFailedException instead.
 * <p>
 * Note: Since framework version 1.5.0.0 it is recommended to use
 * InvalidAttributeValueException in case that credential change operation failed.
 * Please indicate credential as an affected attribute in the exception.
 *
 * @see javax.security.auth.login.CredentialException
 */
public class InvalidPasswordException extends InvalidCredentialException {

    private static final long serialVersionUID = 1L;

    /**
     * @see ConnectorException#ConnectorException()
     */
    public InvalidPasswordException() {
        super();
    }

    /**
     * @see ConnectorException#ConnectorException(String)
     */
    public InvalidPasswordException(String message) {
        super(message);
    }

    /**
     * @see ConnectorException#ConnectorException(Throwable)
     */
    public InvalidPasswordException(Throwable ex) {
        super(ex);
    }

    /**
     * @see ConnectorException#ConnectorException(String, Throwable)
     */
    public InvalidPasswordException(String message, Throwable ex) {
        super(message, ex);
    }
}
