/**
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2011-2013 Tirasa. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License"). You may not use this file
 * except in compliance with the License.
 *
 * You can obtain a copy of the License at https://oss.oracle.com/licenses/CDDL
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at https://oss.oracle.com/licenses/CDDL.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.framework.common.exceptions;

/**
 * Base exception for all Input-Output (I/O-related) exceptions,
 * including instance connection failure, socket error and so forth.
 */
public class ConnectorIOException extends ConnectorException {
    
    private static final long serialVersionUID = 1L;

    public ConnectorIOException() {
        super();
    }
    
    public ConnectorIOException(String msg) {
        super(msg);
    }
    
    public ConnectorIOException(Throwable ex) {
        super(ex);
    }

    public ConnectorIOException(String message, Throwable ex) {
        super(message, ex);
    }
}
