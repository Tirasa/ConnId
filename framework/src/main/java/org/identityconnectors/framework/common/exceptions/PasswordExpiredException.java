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

import org.identityconnectors.framework.api.operations.AuthenticationApiOp;
import org.identityconnectors.framework.common.objects.Uid;

/**
 * Thrown when a password credential is invalid.
 */
public class PasswordExpiredException extends InvalidPasswordException {

    private static final long serialVersionUID = 1L;

    private Uid _uid;
    
    public PasswordExpiredException() {
        super();
    }
    
    public PasswordExpiredException(String message) {
        super(message);
    }

    public PasswordExpiredException(Throwable ex) {
        super(ex);
    }

    public PasswordExpiredException(String message, Throwable ex) {
        super(message, ex);
    }
    
    public Uid getUid() {
        return _uid;
    }
    
    /**
     * Sets the Uid. Connectors who throw this exception from their
     * {@link AuthenticationApiOp} should set the account Uid if available.
     * @param uid The uid.
     * @return A reference to this.
     */
    public PasswordExpiredException initUid(Uid uid) {
        _uid = uid;
        return this;
    }
}
