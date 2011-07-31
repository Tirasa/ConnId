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
package org.identityconnectors.framework.api.operations;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.Uid;

public interface AuthenticationApiOp extends APIOperation {

    /**
     * Most basic authentication available.
     * @param objectClass The object class to use for authenticate.
     * Will typically be an account. Must not be null.
     * @param username
     *            string that represents the account or user id.
     * @param password
     *            string that represents the password for the account or user.
     * @param options
     *            additional options that impact the way this operation is run.
     *            May be null.
     * @return Uid The uid of the account that was used to authenticate
     * @throws RuntimeException
     *             iff the credentials do not pass authentication otherwise
     *             nothing.
     */
    public Uid authenticate(ObjectClass objectClass, String username, 
            GuardedString password, OperationOptions options);
}
