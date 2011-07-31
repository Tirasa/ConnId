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
package org.identityconnectors.contract.exceptions;

/**
 * Generic Contract Tests exception. Base class for all contract tests exceptions.
 */
public class ContractException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ContractException() {
        super();
    }

    /**
     * Sets a message for the {@link Exception}.
     *  
     * @param message
     *            passed to the {@link RuntimeException} message.
     */
    public ContractException(String message) {
        super(message);
    }

    /**
     * Sets the stack trace to the original exception, so this exception can
     * masquerade as the original only be a {@link RuntimeException}.
     * 
     * @param originalException
     *            the original exception adapted to {@link RuntimeException}.
     */
    public ContractException(Throwable originalException) {
        super(originalException);
    }

    /**
     * Sets the stack trace to the original exception, so this exception can
     * masquerade as the original only be a {@link RuntimeException}.
     * 
     * @param message
     * @param originalException
     *            the original exception adapted to {@link RuntimeException}.
     */
    public ContractException(String message, Throwable originalException) {
        super(message, originalException);
    }

    /**
     * Re-throw the original exception.
     * 
     * @throws Exception
     *             throws the original passed in the constructor.
     */
    public void rethrow() throws Throwable {
        throw (getCause() == null) ? this : getCause();
    }

    /**
     * If {@link Exception} parameter passed in is a {@link RuntimeException} it
     * is simply returned. Otherwise the {@link Exception} is wrapped in a
     * <code>ContractException</code> and returned.
     * 
     * @param ex
     *            Exception to wrap or cast and return.
     * @return a <code>RuntimeException</code> that either 
     *           <i>is</i> the specified exception
     *            or <i>contains</i> the specified exception. 
     */
    public static RuntimeException wrap(Throwable ex) {
        // make sure to just throw Errors don't return them..
        if (ex instanceof Error) {
            throw (Error) ex;
        }
        // don't bother to wrap a exception that is already a runtime..
        if (ex instanceof RuntimeException) {
            return (RuntimeException) ex;
        }
        return new ContractException(ex);
    }
}
