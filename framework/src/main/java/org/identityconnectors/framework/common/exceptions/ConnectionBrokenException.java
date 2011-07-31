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
 * http://IdentityConnectors.dev.java.net/legal/license.txt
 * See the License for the specific language governing permissions and limitations 
 * under the License. 
 * 
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at identityconnectors/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package org.identityconnectors.framework.common.exceptions;
/**
 * Thrown when a connection to a target resource instance
 * fails during an operation.
 * An instance of <code>ConnectionBrokenException</code> 
 * generally wraps the native exception (or describes the native error)
 * returned by the target resource.
 */
public class ConnectionBrokenException extends ConnectorIOException {
    
    private static final long serialVersionUID = 1L;

    public ConnectionBrokenException() {
        super();
    }
    
    public ConnectionBrokenException(String msg) {
        super(msg);
    }
    
    public ConnectionBrokenException(Throwable ex) {
        super(ex);
    }

    public ConnectionBrokenException(String message, Throwable ex) {
        super(message, ex);
    }

}
