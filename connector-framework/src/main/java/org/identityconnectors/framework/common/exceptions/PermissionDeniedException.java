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

import org.identityconnectors.framework.spi.Connector;

/**
 * Thrown when the target resource will not allow a {@link Connector} to perform
 * a particular operation. An instance of <code>PermissionDeniedException</code>
 * generally describes a native error returned by (or wraps a native exception
 * thrown by) the target resource.
 */
public class PermissionDeniedException extends ConnectorSecurityException {

    private static final long serialVersionUID = 1L;

    /**
     * @see ConnectorException#ConnectorException()
     */
    public PermissionDeniedException() {
        super();
    }

    /**
     * @see ConnectorException#ConnectorException(String)
     */
    public PermissionDeniedException(String message) {
        super(message);
    }

    /**
     * @see ConnectorException#ConnectorException(Throwable)
     */
    public PermissionDeniedException(Throwable ex) {
        super(ex);
    }

    /**
     * @see ConnectorException#ConnectorException(String, Throwable)
     */
    public PermissionDeniedException(String message, Throwable ex) {
        super(message, ex);
    }
}
