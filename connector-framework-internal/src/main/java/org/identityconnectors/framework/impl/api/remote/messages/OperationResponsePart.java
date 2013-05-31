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
package org.identityconnectors.framework.impl.api.remote.messages;

/**
 * Represents one part of a response. Most operations return just a single
 * response part, followed by a OperationResponseEnd. The one exception is
 * Search, which returns multiple parts.
 */
public class OperationResponsePart implements Message {
    private Throwable exception;
    private Object result;

    public OperationResponsePart(Throwable ex, Object result) {
        exception = ex;
        this.result = result;
    }

    public Throwable getException() {
        return exception;
    }

    public Object getResult() {
        return result;
    }
}
